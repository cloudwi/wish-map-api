package com.mindbridge.wishmap.context.place.application

import com.mindbridge.wishmap.context.place.api.dto.*
import com.mindbridge.wishmap.context.place.domain.Place
import com.mindbridge.wishmap.context.place.domain.PlaceCategoryRepository
import com.mindbridge.wishmap.context.place.domain.PlaceRepository
import com.mindbridge.wishmap.context.place.domain.PriceRange
import com.mindbridge.wishmap.context.place.infrastructure.NaverSearchService
import com.mindbridge.wishmap.context.review.application.CommentService
import com.mindbridge.wishmap.context.review.domain.Comment
import com.mindbridge.wishmap.context.review.domain.CommentImage
import com.mindbridge.wishmap.context.review.domain.CommentRepository
import com.mindbridge.wishmap.context.review.domain.CommentTag
import com.mindbridge.wishmap.context.review.domain.Visit
import com.mindbridge.wishmap.context.review.domain.VisitRepository
import com.mindbridge.wishmap.context.identity.domain.User
import com.mindbridge.wishmap.common.error.DuplicateResourceException
import com.mindbridge.wishmap.common.error.ResourceNotFoundException
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

import kotlin.math.*

@Service
class PlaceService(
    private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(PlaceService::class.java),
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val visitRepository: VisitRepository,
    private val naverSearchService: NaverSearchService,
    private val placeCategoryRepository: PlaceCategoryRepository
) {

    companion object {
        private const val VISIT_DISTANCE_LIMIT_METERS = 100.0
        private const val EARTH_RADIUS_METERS = 6_371_000.0

        // 네이버 지역검색 category 최상위 → 앱 place_categories id 매핑.
        // Why: 네이버는 공식 카테고리 목록을 공개하지 않아(가변적) 완전 커버 불가.
        //      주요 값만 exact + 키워드 fallback으로 처리, 매핑 실패는 null + WARN 로그로 모니터.
        private val NAVER_CATEGORY_EXACT_MAP = mapOf(
            // 음식점 (id=1)
            "한식" to 1L, "중식" to 1L, "양식" to 1L, "일식" to 1L,
            "분식" to 1L, "아시아음식" to 1L, "퓨전요리" to 1L,
            "뷔페" to 1L, "치킨" to 1L, "패스트푸드" to 1L,
            "술집" to 1L, "도시락" to 1L, "간식" to 1L,
            // 카페,디저트 (id=2)
            "카페,디저트" to 2L, "빵,케익,샌드위치" to 2L,
            // 쇼핑,유통 (id=3)
            "마트" to 3L, "백화점" to 3L, "의류" to 3L, "신발" to 3L,
            // 생활,편의 (id=4)
            "편의점" to 4L, "세탁" to 4L, "미용실" to 4L, "부동산" to 4L,
            // 여행,숙박 (id=5)
            "숙박" to 5L, "펜션" to 5L, "모텔" to 5L, "호텔" to 5L,
            // 문화,예술 (id=6)
            "영화관" to 6L, "박물관" to 6L, "공연장" to 6L,
            // 교육,학문 (id=7)
            "학원" to 7L, "학교" to 7L, "도서관" to 7L,
            // 의료,건강 (id=8)
            "병원" to 8L, "의원" to 8L, "약국" to 8L,
        )
    }

    private fun loadCategoryNameMap(): Map<Long, String> =
        placeCategoryRepository.findActiveBasic().associate { it.id to it.name }

    /**
     * 네이버 지역검색 category 원문에서 앱 place_categories.id를 해석.
     * 예) "한식>육류,고기요리" → 1 (음식점)
     * 매칭 실패 시 null 반환 + WARN 로그 (운영에서 주기적 확인 후 매핑 보강).
     */
    private fun resolvePlaceCategoryId(naverCategory: String?): Long? {
        if (naverCategory.isNullOrBlank()) return null
        val top = naverCategory.substringBefore('>').trim()
        if (top.isEmpty()) return null

        NAVER_CATEGORY_EXACT_MAP[top]?.let { return it }

        return when {
            top.contains("카페") || top.contains("디저트") || top.contains("빵") -> 2L
            top.contains("마트") || top.contains("백화점") || top.contains("의류")
                || top.contains("쇼핑") || top.contains("잡화") -> 3L
            top.contains("편의") || top.contains("세탁") || top.contains("미용")
                || top.contains("부동산") || top.contains("철물") -> 4L
            top.contains("숙박") || top.contains("호텔") || top.contains("펜션")
                || top.contains("모텔") || top.contains("관광") || top.contains("리조트") -> 5L
            top.contains("영화") || top.contains("박물관") || top.contains("공연")
                || top.contains("미술") || top.contains("전시") -> 6L
            top.contains("학원") || top.contains("학교") || top.contains("도서")
                || top.contains("교육") -> 7L
            top.contains("병원") || top.contains("의원") || top.contains("약국")
                || top.contains("치과") || top.contains("한의원") -> 8L
            top.matches(Regex(".*(식|요리|뷔페|술집|치킨|피자|면|국|탕|찌개|구이|분식|고기).*")) -> 1L
            else -> {
                log.warn("네이버 카테고리 매핑 실패: naverCategory={}", naverCategory)
                null
            }
        }
    }

    @Transactional(readOnly = true)
    fun getPlaces(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        tags: List<String>?,
        pageable: Pageable
    ): Slice<PlaceListResponse> {
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
        val page = if (effectiveTags != null) {
            placeRepository.findByLocationBoundsWithTags(
                minLat, maxLat, minLng, maxLng, priceRange, placeCategoryId, effectiveTags, pageable
            )
        } else {
            placeRepository.findByLocationBoundsWithFilters(
                minLat, maxLat, minLng, maxLng, priceRange, placeCategoryId, pageable
            )
        }
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    // Keyset + sortBy=visits. 복합 커서 (visitCount, id).
    @Transactional(readOnly = true)
    fun getPlacesByCursorSortByVisits(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        cursorVisitCount: Long?,
        cursorId: Long?,
        size: Int
    ): Slice<PlaceListResponse> {
        val effectiveSearch = search?.takeIf { it.isNotBlank() }
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
        val hasTagFilter = effectiveTags != null
        val tagsParam = effectiveTags ?: listOf("")

        val slice = placeRepository.findWithFiltersSortByVisitsCursor(
            placeCategoryId, effectiveSearch, priceRange, hasTagFilter, tagsParam,
            cursorVisitCount, cursorId, PageRequest.of(0, size)
        )
        val visitCountMap = batchVisitCounts(slice.content)
        val weeklyChampionMap = batchWeeklyChampions(slice.content)
        val lastVisitMap = batchLastVisitedAt(slice.content)
        return slice.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    // Keyset(cursor) 기반 기본 최신순 정렬 조회.
    // sortBy=visits는 위 별도 메서드, recentVisit/distance는 복잡도 대비 이득이 적어 offset 유지.
    @Transactional(readOnly = true)
    fun getPlacesByCursor(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        size: Int
    ): Slice<PlaceListResponse> {
        val effectiveSearch = search?.takeIf { it.isNotBlank() }
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
        val hasTagFilter = effectiveTags != null
        // hasTagFilter=false인 경우 :tags 파라미터는 사용되지 않지만 Hibernate 바인딩용 더미 값 전달.
        val tagsParam = effectiveTags ?: listOf("")

        val slice = placeRepository.findWithFiltersByCursor(
            placeCategoryId, effectiveSearch, priceRange, hasTagFilter, tagsParam,
            cursorCreatedAt, cursorId, PageRequest.of(0, size)
        )
        val visitCountMap = batchVisitCounts(slice.content)
        val weeklyChampionMap = batchWeeklyChampions(slice.content)
        val lastVisitMap = batchLastVisitedAt(slice.content)
        return slice.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPlacesWithFilters(
        category: String?,
        search: String?,
        sort: String?,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        tags: List<String>?,
        pageable: Pageable,
        userLat: Double? = null,
        userLng: Double? = null
    ): Slice<PlaceListResponse> {
        val effectiveSearch = search?.takeIf { it.isNotBlank() }
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

        val page = when (sort) {
            "visits" -> if (effectiveTags != null) {
                placeRepository.findWithFiltersSortByVisitsWithTags(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
            } else {
                placeRepository.findWithFiltersSortByVisits(placeCategoryId, effectiveSearch, priceRange, pageable)
            }
            "recentVisit" -> placeRepository.findWithFiltersSortByRecentVisit(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
            "distance" -> {
                require(userLat != null && userLng != null) { "userLat and userLng are required for distance sort" }
                placeRepository.findWithFiltersSortByDistance(
                    placeCategoryId, effectiveSearch, priceRange?.name, userLat, userLng, pageable
                )
            }
            else -> placeRepository.findWithFilters(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
        }

        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPlacesByMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, priceRange: PriceRange?, pageable: Pageable
    ): Slice<PlaceListResponse> {
        if (memberIds.isEmpty()) return org.springframework.data.domain.SliceImpl(emptyList())
        val page = placeRepository.findByLocationBoundsAndMembers(minLat, maxLat, minLng, maxLng, memberIds, priceRange, pageable)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    private fun batchVisitCounts(places: List<Place>): Map<Long, Long> {
        if (places.isEmpty()) return emptyMap()
        return placeRepository.countVisitsByPlaces(places)
            .associate { row -> (row[0] as Long) to (row[1] as Long) }
    }

    private fun batchLastVisitedAt(places: List<Place>): Map<Long, java.time.LocalDateTime> {
        if (places.isEmpty()) return emptyMap()
        val ids = places.map { it.id }
        return visitRepository.findLastVisitDatesByPlaceIds(ids)
            .associate { row -> (row[0] as Long) to (row[1] as java.time.LocalDateTime) }
    }

    // 최근 7일 롤링 윈도우 (한국 시간 기준)
    private fun getWeekRange(): Pair<LocalDateTime, LocalDateTime> {
        val koreaZone = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(koreaZone)
        val sevenDaysAgo = now.minusDays(7)
        return Pair(sevenDaysAgo, now)
    }

    // 주간 방문왕 캐시 (1시간 TTL)
    private var championCache: Map<Long, String> = emptyMap()
    private var championCacheTime: Long = 0
    private val CHAMPION_CACHE_TTL = 1000L * 60 * 60 // 1시간

    // 조회 대상 식당의 주간 방문왕을 배치로 조회 (캐시 활용)
    private fun batchWeeklyChampions(places: List<Place>): Map<Long, String> {
        if (places.isEmpty()) return emptyMap()

        val now = System.currentTimeMillis()
        if (now - championCacheTime > CHAMPION_CACHE_TTL) {
            refreshChampionCache()
        }

        val placeIds = places.map { it.id }.toSet()
        return championCache.filterKeys { it in placeIds }
    }

    private fun refreshChampionCache() {
        val (weekStart, weekEnd) = getWeekRange()
        val results = visitRepository.findAllWeeklyChampions(weekStart, weekEnd)
        val newCache = mutableMapOf<Long, String>()
        for (row in results) {
            val placeId = row[0] as Long
            val nickname = row[1] as String
            newCache.putIfAbsent(placeId, nickname)
        }
        championCache = newCache
        championCacheTime = System.currentTimeMillis()
    }

    @Transactional(readOnly = true)
    fun getPlaceDetail(id: Long, userId: Long?): PlaceDetailResponse {
        val place = placeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Place not found: $id") }

        val visitCount = visitRepository.countByPlace(place)
        val commentCount = commentRepository.countByPlaceAndIsDeletedFalse(place)

        val lastVisit = visitRepository.findFirstByPlaceOrderByCreatedAtDesc(place)
        val user = userId?.let { userRepository.findById(it).orElse(null) }
        val today = LocalDate.now()
        val isVisited = user != null &&
            visitRepository.existsByPlaceAndUserAndCreatedAtBetween(
                place, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )

        return PlaceDetailResponse(
            id = place.id,
            name = place.name,
            lat = place.lat,
            lng = place.lng,
            naverPlaceId = place.naverPlaceId,
            category = place.category,
            description = place.description,
            thumbnailImage = place.thumbnailImage,
            images = emptyList(),
            suggestedBy = UserSummary(
                id = place.suggestedBy.id,
                nickname = place.suggestedBy.nickname,
                profileImage = place.suggestedBy.profileImage
            ),
            visitCount = visitCount,
            commentCount = commentCount,
            isVisited = isVisited,
            priceRange = place.priceRange?.name,
            placeCategoryId = place.placeCategoryId,
            lastVisitedAt = lastVisit?.createdAt,
            createdAt = place.createdAt,
            updatedAt = place.updatedAt
        )
    }

    private fun createPlaceFromQuickVisit(request: QuickVisitRequest, user: User): Place {
        val fallback = request.category?.takeIf { it.isNotBlank() }?.let { "${request.name} $it" }
        val thumbnail = naverSearchService.searchThumbnail(request.name, fallback)
        val resolvedCategoryId = request.placeCategoryId ?: resolvePlaceCategoryId(request.category)
        return placeRepository.save(
            Place(
                name = request.name,
                lat = request.lat,
                lng = request.lng,
                naverPlaceId = request.naverPlaceId,
                category = request.category,
                priceRange = request.priceRange,
                placeCategoryId = resolvedCategoryId,
                thumbnailImage = thumbnail,
                suggestedBy = user
            )
        )
    }

    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    @Transactional
    fun createPlace(userId: Long, request: CreatePlaceRequest): PlaceDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        if (request.naverPlaceId != null && placeRepository.existsByNaverPlaceId(request.naverPlaceId)) {
            throw IllegalArgumentException("이미 등록된 장소입니다")
        }

        val fallback = request.category?.takeIf { it.isNotBlank() }?.let { "${request.name} $it" }
        val thumbnail = request.thumbnailImage ?: naverSearchService.searchThumbnail(request.name, fallback)
        val place = Place(
            name = request.name,
            lat = request.lat,
            lng = request.lng,
            naverPlaceId = request.naverPlaceId,
            category = request.category,
            placeCategoryId = resolvePlaceCategoryId(request.category),
            description = request.description,
            thumbnailImage = thumbnail,
            suggestedBy = user
        )

        val saved = placeRepository.save(place)
        return getPlaceDetail(saved.id, userId)
    }

    @Transactional(readOnly = true)
    fun getMyPlaces(userId: Long, pageable: Pageable): Slice<PlaceListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val slice = placeRepository.findBySuggestedBy(user, pageable)
        return enrichMyPlaces(user, slice)
    }

    // Keyset(cursor) 기반 내 제보 목록 조회.
    @Transactional(readOnly = true)
    fun getMyPlacesByCursor(
        userId: Long,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        size: Int
    ): Slice<PlaceListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val slice = placeRepository.findBySuggestedByCursor(user, cursorCreatedAt, cursorId, PageRequest.of(0, size))
        return enrichMyPlaces(user, slice)
    }

    private fun enrichMyPlaces(user: User, slice: Slice<Place>): Slice<PlaceListResponse> {
        val visitCountMap = batchVisitCounts(slice.content)
        val weeklyChampionMap = batchWeeklyChampions(slice.content)
        val placeIds = slice.content.map { it.id }
        val lastVisitMap = if (placeIds.isNotEmpty()) {
            visitRepository.findLastVisitDatesByUserAndPlaceIds(user, placeIds)
                .associate { (it[0] as Long) to (it[1] as java.time.LocalDateTime) }
        } else emptyMap()
        return slice.map { place ->
            place.toListResponse(
                visitCount = visitCountMap[place.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[place.id],
                lastVisitedAt = lastVisitMap[place.id]
            )
        }
    }

    @Transactional
    fun quickVisit(userId: Long, request: QuickVisitRequest): QuickVisitResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        var isNew = false
        val place = if (request.naverPlaceId != null) {
            placeRepository.findByNaverPlaceId(request.naverPlaceId) ?: run {
                isNew = true
                createPlaceFromQuickVisit(request, user)
            }
        } else if (request.placeCategoryId != null) {
            // 커스텀 장소: 같은 카테고리 + 100m 이내 기존 장소 재사용
            val radiusDeg = VISIT_DISTANCE_LIMIT_METERS / 111000.0
            val nearby = placeRepository.findNearbyCustomByCategory(
                request.placeCategoryId,
                request.lat - radiusDeg, request.lat + radiusDeg,
                request.lng - radiusDeg, request.lng + radiusDeg
            ).firstOrNull { haversineDistance(request.lat, request.lng, it.lat, it.lng) <= VISIT_DISTANCE_LIMIT_METERS }
            nearby ?: run {
                isNew = true
                createPlaceFromQuickVisit(request, user)
            }
        } else {
            isNew = true
            createPlaceFromQuickVisit(request, user)
        }

        val distance = haversineDistance(request.userLat, request.userLng, place.lat, place.lng)
        if (distance > VISIT_DISTANCE_LIMIT_METERS) {
            throw IllegalArgumentException("장소에서 100m 이내에서만 방문 인증이 가능합니다")
        }

        val today = LocalDate.now()
        if (visitRepository.existsByPlaceAndUserAndCreatedAtBetween(
                place, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )) {
            throw DuplicateResourceException("오늘 이미 방문 인증한 장소입니다")
        }

        val visit = Visit(place = place, user = user, rating = request.rating, priceRange = request.priceRange)
        visitRepository.save(visit)

        // 리뷰 (한줄평 + 태그 + 이미지) 처리
        if (!request.comment.isNullOrBlank() || request.tags.isNotEmpty() || request.imageUrls.isNotEmpty()) {
            val comment = Comment(
                place = place,
                user = user,
                content = request.comment ?: ""
            )
            request.tags.forEach { tag ->
                comment.tags.add(CommentTag(comment = comment, tag = tag, category = CommentService.resolveTagCategory(tag)))
            }
            request.imageUrls.forEachIndexed { index, url ->
                comment.images.add(CommentImage(comment = comment, imageUrl = url, displayOrder = index))
            }
            commentRepository.save(comment)
        }

        // 가격대 캐시 업데이트
        updateCachedPriceRange(place)

        // thumbnail이 없는 기존 장소에 이미지 채우기
        if (place.thumbnailImage == null) {
            val fallback = place.category?.takeIf { it.isNotBlank() }?.let { "${place.name} $it" }
            val thumbnail = naverSearchService.searchThumbnail(place.name, fallback)
            if (thumbnail != null) {
                place.thumbnailImage = thumbnail
                placeRepository.save(place)
            }
        }

        log.info("방문인증: userId={}, placeId={}, isNew={}, name={}", userId, place.id, isNew, place.name)
        return QuickVisitResponse(placeId = place.id, visited = true, isNew = isNew)
    }

    private fun updateCachedPriceRange(place: Place) {
        val results = visitRepository.findPriceRangesByPlaces(listOf(place))
        if (results.isNotEmpty()) {
            val topPriceRange = results[0][1] as? PriceRange
            if (topPriceRange != null) {
                place.priceRange = topPriceRange
                placeRepository.save(place)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getWeeklyTopPlaces(limit: Int = 3): List<WeeklyTopPlace> {
        val (weekStart, weekEnd) = getWeekRange()
        val places = placeRepository.findWeeklyTopPlaces(
            weekStart, weekEnd, PageRequest.of(0, limit)
        )
        val visitCountMap = batchVisitCounts(places)
        return places.map { r ->
            WeeklyTopPlace(
                id = r.id,
                name = r.name,
                category = r.category,
                thumbnailImage = r.thumbnailImage,
                visitCount = visitCountMap[r.id] ?: 0L,
                placeCategoryId = r.placeCategoryId
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPopularPlaces(limit: Int = 5): List<PopularPlace> {
        val places = placeRepository.findPopularPlaces(PageRequest.of(0, limit))
        val visitCountMap = batchVisitCounts(places)
        return places.map { r ->
            PopularPlace(
                id = r.id,
                name = r.name,
                category = r.category,
                thumbnailImage = r.thumbnailImage,
                totalVisitCount = visitCountMap[r.id] ?: 0L,
                placeCategoryId = r.placeCategoryId
            )
        }
    }

    @Transactional(readOnly = true)
    fun getCategorySummary(): List<CategorySummary> {
        return placeRepository.countByPlaceCategory().map { row ->
            val categoryId = row[0] as Long
            val count = row[1] as Long
            CategorySummary(
                placeCategoryId = categoryId,
                name = "",  // will be filled by frontend from cached categories
                placeCount = count
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPlaceStats(naverPlaceId: String, userId: Long? = null): PlaceStatsResponse {
        val place = placeRepository.findByNaverPlaceId(naverPlaceId)
            ?: return PlaceStatsResponse(
                placeId = 0,
                visitCount = 0,
                avgRating = null,
                visitedToday = false,
                priceRange = null,
                placeCategoryId = null,
                recentReviews = emptyList()
            )

        // visit 통계를 단일 쿼리로 조회
        val visitCount = visitRepository.countByPlace(place)
        val lastVisit = visitRepository.findFirstByPlaceOrderByCreatedAtDesc(place)

        // 댓글은 @EntityGraph로 user+tags 한 번에 조회 (N+1 방지)
        val recentComments = commentRepository
            .findTop3ByPlaceAndIsDeletedFalseOrderByCreatedAtDesc(place)

        val today = LocalDate.now()
        val visitedToday = if (userId != null) {
            visitRepository.existsByPlaceIdAndUserIdAndCreatedAtBetween(
                place.id, userId, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )
        } else false

        return PlaceStatsResponse(
            placeId = place.id,
            visitCount = visitCount,
            avgRating = null,
            visitedToday = visitedToday,
            priceRange = place.priceRange?.name,
            placeCategoryId = place.placeCategoryId,
            recentReviews = recentComments.map { comment ->
                ReviewSummary(
                    nickname = comment.user.nickname,
                    profileImage = comment.user.profileImage,
                    content = comment.content,
                    tags = comment.tags.map { it.tag },
                    createdAt = comment.createdAt
                )
            },
            lastVisitedAt = lastVisit?.createdAt
        )
    }

}
