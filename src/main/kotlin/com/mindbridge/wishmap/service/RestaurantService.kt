package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.comment.CommentImage
import com.mindbridge.wishmap.domain.comment.CommentTag
import com.mindbridge.wishmap.domain.restaurant.PriceRange
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.restaurant.RestaurantImage
import com.mindbridge.wishmap.domain.restaurant.Visit
import com.mindbridge.wishmap.domain.user.User
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.DuplicateResourceException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

import kotlin.math.*

@Service
class RestaurantService(
    private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(RestaurantService::class.java),
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val visitRepository: VisitRepository,
    private val naverSearchService: NaverSearchService
) {

    companion object {
        private const val VISIT_DISTANCE_LIMIT_METERS = 100.0
        private const val EARTH_RADIUS_METERS = 6_371_000.0
    }

    @Transactional(readOnly = true)
    fun getRestaurants(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        tags: List<String>?,
        pageable: Pageable
    ): Page<RestaurantListResponse> {
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
        val page = if (effectiveTags != null) {
            restaurantRepository.findByLocationBoundsWithTags(
                minLat, maxLat, minLng, maxLng, priceRange, placeCategoryId, effectiveTags, pageable
            )
        } else {
            restaurantRepository.findByLocationBoundsWithFilters(
                minLat, maxLat, minLng, maxLng, priceRange, placeCategoryId, pageable
            )
        }
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id],
                lastVisitedAt = lastVisitMap[restaurant.id]
            )
        }
    }

    @Transactional(readOnly = true)
    fun getRestaurantsWithFilters(
        category: String?,
        search: String?,
        sort: String?,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        tags: List<String>?,
        pageable: Pageable,
        userLat: Double? = null,
        userLng: Double? = null
    ): Page<RestaurantListResponse> {
        val effectiveSearch = search?.takeIf { it.isNotBlank() }
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

        val page = when (sort) {
            "visits" -> restaurantRepository.findWithFiltersSortByVisits(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
            "recentVisit" -> restaurantRepository.findWithFiltersSortByRecentVisit(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
            "distance" -> {
                require(userLat != null && userLng != null) { "userLat and userLng are required for distance sort" }
                restaurantRepository.findWithFiltersSortByDistance(
                    placeCategoryId, effectiveSearch, priceRange?.name, userLat, userLng, pageable
                )
            }
            else -> restaurantRepository.findWithFilters(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
        }

        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id],
                lastVisitedAt = lastVisitMap[restaurant.id]
            )
        }
    }

    @Transactional(readOnly = true)
    fun getRestaurantsByMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, priceRange: PriceRange?, pageable: Pageable
    ): Page<RestaurantListResponse> {
        if (memberIds.isEmpty()) return Page.empty()
        val page = restaurantRepository.findByLocationBoundsAndMembers(minLat, maxLat, minLng, maxLng, memberIds, priceRange, pageable)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val lastVisitMap = batchLastVisitedAt(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id],
                lastVisitedAt = lastVisitMap[restaurant.id]
            )
        }
    }

    private fun batchVisitCounts(restaurants: List<Restaurant>): Map<Long, Long> {
        if (restaurants.isEmpty()) return emptyMap()
        return restaurantRepository.countVisitsByRestaurants(restaurants)
            .associate { row -> (row[0] as Long) to (row[1] as Long) }
    }

    private fun batchLastVisitedAt(restaurants: List<Restaurant>): Map<Long, java.time.LocalDateTime> {
        if (restaurants.isEmpty()) return emptyMap()
        val ids = restaurants.map { it.id }
        return visitRepository.findLastVisitDatesByRestaurantIds(ids)
            .associate { row -> (row[0] as Long) to (row[1] as java.time.LocalDateTime) }
    }

    // 최근 7일 롤링 윈도우 (한국 시간 기준)
    private fun getWeekRange(): Pair<LocalDateTime, LocalDateTime> {
        val koreaZone = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(koreaZone)
        val sevenDaysAgo = now.minusDays(7)
        return Pair(sevenDaysAgo, now)
    }

    // 조회 대상 식당의 주간 방문왕을 배치로 조회
    private fun batchWeeklyChampions(restaurants: List<Restaurant>): Map<Long, String> {
        if (restaurants.isEmpty()) return emptyMap()
        val restaurantIds = restaurants.map { it.id }
        val (weekStart, weekEnd) = getWeekRange()
        val results = visitRepository.findWeeklyChampionsByRestaurantIds(restaurantIds, weekStart, weekEnd)

        val championMap = mutableMapOf<Long, String>()
        for (row in results) {
            val restaurantId = row[0] as Long
            val nickname = row[1] as String
            championMap.putIfAbsent(restaurantId, nickname)
        }
        return championMap
    }

    @Transactional(readOnly = true)
    fun getRestaurantDetail(id: Long, userId: Long?): RestaurantDetailResponse {
        val restaurant = restaurantRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $id") }

        val visitCount = visitRepository.countByRestaurant(restaurant)
        val commentCount = commentRepository.countByRestaurantAndIsDeletedFalse(restaurant)

        val lastVisit = visitRepository.findFirstByRestaurantOrderByCreatedAtDesc(restaurant)
        val user = userId?.let { userRepository.findById(it).orElse(null) }
        val today = LocalDate.now()
        val isVisited = user != null &&
            visitRepository.existsByRestaurantAndUserAndCreatedAtBetween(
                restaurant, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )

        return RestaurantDetailResponse(
            id = restaurant.id,
            name = restaurant.name,
            lat = restaurant.lat,
            lng = restaurant.lng,
            naverPlaceId = restaurant.naverPlaceId,
            category = restaurant.category,
            description = restaurant.description,
            thumbnailImage = restaurant.thumbnailImage,
            images = restaurant.images.sortedBy { it.displayOrder }.map { it.imageUrl },
            suggestedBy = UserSummary(
                id = restaurant.suggestedBy.id,
                nickname = restaurant.suggestedBy.nickname,
                profileImage = restaurant.suggestedBy.profileImage
            ),
            visitCount = visitCount,
            commentCount = commentCount,
            isVisited = isVisited,
            priceRange = restaurant.priceRange?.name,
            placeCategoryId = restaurant.placeCategoryId,
            lastVisitedAt = lastVisit?.createdAt,
            createdAt = restaurant.createdAt,
            updatedAt = restaurant.updatedAt
        )
    }

    @Transactional
    fun verifyVisit(restaurantId: Long, userId: Long, request: VisitVerifyRequest): Boolean {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val today = LocalDate.now()
        if (visitRepository.existsByRestaurantAndUserAndCreatedAtBetween(
                restaurant, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )) {
            throw DuplicateResourceException("오늘 이미 방문 인증한 장소입니다")
        }

        val distance = haversineDistance(request.lat, request.lng, restaurant.lat, restaurant.lng)
        if (distance > VISIT_DISTANCE_LIMIT_METERS) {
            throw IllegalArgumentException("장소에서 100m 이내에서만 방문 인증이 가능합니다")
        }

        visitRepository.save(Visit(restaurant = restaurant, user = user, priceRange = restaurant.priceRange ?: PriceRange.RANGE_10K))
        return true
    }

    private fun createRestaurantFromQuickVisit(request: QuickVisitRequest, user: User): Restaurant {
        val thumbnail = naverSearchService.searchThumbnail(request.name)
        return restaurantRepository.save(
            Restaurant(
                name = request.name,
                lat = request.lat,
                lng = request.lng,
                naverPlaceId = request.naverPlaceId,
                category = request.category,
                priceRange = request.priceRange,
                placeCategoryId = request.placeCategoryId,
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
    fun createRestaurant(userId: Long, request: CreateRestaurantRequest): RestaurantDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        if (request.naverPlaceId != null && restaurantRepository.existsByNaverPlaceId(request.naverPlaceId)) {
            throw IllegalArgumentException("이미 등록된 장소입니다")
        }

        val thumbnail = request.thumbnailImage ?: naverSearchService.searchThumbnail(request.name)
        val restaurant = Restaurant(
            name = request.name,
            lat = request.lat,
            lng = request.lng,
            naverPlaceId = request.naverPlaceId,
            category = request.category,
            description = request.description,
            thumbnailImage = thumbnail,
            suggestedBy = user
        )

        val saved = restaurantRepository.save(restaurant)
        return getRestaurantDetail(saved.id, userId)
    }

    @Transactional(readOnly = true)
    fun getMyRestaurants(userId: Long, pageable: Pageable): Page<RestaurantListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val page = restaurantRepository.findBySuggestedBy(user, pageable)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        val restaurantIds = page.content.map { it.id }
        val lastVisitMap = if (restaurantIds.isNotEmpty()) {
            visitRepository.findLastVisitDatesByUserAndRestaurantIds(user, restaurantIds)
                .associate { (it[0] as Long) to (it[1] as java.time.LocalDateTime) }
        } else emptyMap()
        return page.map { restaurant ->
            restaurant.toListResponse(
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id],
                lastVisitedAt = lastVisitMap[restaurant.id]
            )
        }
    }

    @Transactional
    fun quickVisit(userId: Long, request: QuickVisitRequest): QuickVisitResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        var isNew = false
        val restaurant = if (request.naverPlaceId != null) {
            restaurantRepository.findByNaverPlaceId(request.naverPlaceId) ?: run {
                isNew = true
                createRestaurantFromQuickVisit(request, user)
            }
        } else if (request.placeCategoryId != null) {
            // 커스텀 장소: 같은 카테고리 + 100m 이내 기존 장소 재사용
            val radiusDeg = VISIT_DISTANCE_LIMIT_METERS / 111000.0
            val nearby = restaurantRepository.findNearbyCustomByCategory(
                request.placeCategoryId,
                request.lat - radiusDeg, request.lat + radiusDeg,
                request.lng - radiusDeg, request.lng + radiusDeg
            ).firstOrNull { haversineDistance(request.lat, request.lng, it.lat, it.lng) <= VISIT_DISTANCE_LIMIT_METERS }
            nearby ?: run {
                isNew = true
                createRestaurantFromQuickVisit(request, user)
            }
        } else {
            isNew = true
            createRestaurantFromQuickVisit(request, user)
        }

        val distance = haversineDistance(request.userLat, request.userLng, restaurant.lat, restaurant.lng)
        if (distance > VISIT_DISTANCE_LIMIT_METERS) {
            throw IllegalArgumentException("장소에서 100m 이내에서만 방문 인증이 가능합니다")
        }

        val today = LocalDate.now()
        if (visitRepository.existsByRestaurantAndUserAndCreatedAtBetween(
                restaurant, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )) {
            throw DuplicateResourceException("오늘 이미 방문 인증한 장소입니다")
        }

        val visit = Visit(restaurant = restaurant, user = user, rating = request.rating, priceRange = request.priceRange)
        visitRepository.save(visit)

        // 리뷰 (한줄평 + 태그 + 이미지) 처리
        if (!request.comment.isNullOrBlank() || request.tags.isNotEmpty() || request.imageUrls.isNotEmpty()) {
            val comment = Comment(
                restaurant = restaurant,
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
        updateCachedPriceRange(restaurant)

        // thumbnail이 없는 기존 장소에 이미지 채우기
        if (restaurant.thumbnailImage == null) {
            val thumbnail = naverSearchService.searchThumbnail(restaurant.name)
            if (thumbnail != null) {
                restaurant.thumbnailImage = thumbnail
                restaurantRepository.save(restaurant)
            }
        }

        log.info("방문인증: userId={}, restaurantId={}, isNew={}, name={}", userId, restaurant.id, isNew, restaurant.name)
        return QuickVisitResponse(restaurantId = restaurant.id, visited = true, isNew = isNew)
    }

    private fun updateCachedPriceRange(restaurant: Restaurant) {
        val results = visitRepository.findPriceRangesByRestaurants(listOf(restaurant))
        if (results.isNotEmpty()) {
            val topPriceRange = results[0][1] as? PriceRange
            if (topPriceRange != null) {
                restaurant.priceRange = topPriceRange
                restaurantRepository.save(restaurant)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getWeeklyTopRestaurants(limit: Int = 3): List<WeeklyTopRestaurant> {
        val (weekStart, weekEnd) = getWeekRange()
        val restaurants = restaurantRepository.findWeeklyTopRestaurants(
            weekStart, weekEnd, PageRequest.of(0, limit)
        )
        val visitCountMap = batchVisitCounts(restaurants)
        return restaurants.map { r ->
            WeeklyTopRestaurant(
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
    fun getPopularRestaurants(limit: Int = 5): List<PopularRestaurant> {
        val restaurants = restaurantRepository.findPopularRestaurants(PageRequest.of(0, limit))
        val visitCountMap = batchVisitCounts(restaurants)
        return restaurants.map { r ->
            PopularRestaurant(
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
        return restaurantRepository.countByPlaceCategory().map { row ->
            val categoryId = row[0] as Long
            val count = row[1] as Long
            CategorySummary(
                placeCategoryId = categoryId,
                name = "",  // will be filled by frontend from cached categories
                restaurantCount = count
            )
        }
    }

    @Transactional(readOnly = true)
    fun getPlaceStats(naverPlaceId: String, userId: Long? = null): PlaceStatsResponse {
        val restaurant = restaurantRepository.findByNaverPlaceId(naverPlaceId)
            ?: return PlaceStatsResponse(
                restaurantId = 0,
                visitCount = 0,
                avgRating = null,
                visitedToday = false,
                priceRange = null,
                placeCategoryId = null,
                recentReviews = emptyList()
            )

        val visitCount = visitRepository.countByRestaurant(restaurant)
        val avgRating = visitRepository.findAvgRatingByRestaurant(restaurant)
        val lastVisit = visitRepository.findFirstByRestaurantOrderByCreatedAtDesc(restaurant)
        val recentComments = commentRepository
            .findTop3ByRestaurantAndIsDeletedFalseOrderByCreatedAtDesc(restaurant)

        val today = LocalDate.now()
        val visitedToday = userId?.let { uid ->
            val user = userRepository.findById(uid).orElse(null)
            user != null && visitRepository.existsByRestaurantAndUserAndCreatedAtBetween(
                restaurant, user, today.atStartOfDay(), today.atTime(LocalTime.MAX)
            )
        } ?: false

        return PlaceStatsResponse(
            restaurantId = restaurant.id,
            visitCount = visitCount,
            avgRating = avgRating,
            visitedToday = visitedToday,
            priceRange = restaurant.priceRange?.name,
            placeCategoryId = restaurant.placeCategoryId,
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
