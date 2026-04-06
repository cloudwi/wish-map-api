package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.comment.CommentImage
import com.mindbridge.wishmap.domain.comment.CommentTag
import com.mindbridge.wishmap.domain.restaurant.Like
import com.mindbridge.wishmap.domain.restaurant.LikeGroup
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
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val likeGroupRepository: LikeGroupRepository,
    private val commentRepository: CommentRepository,
    private val visitRepository: VisitRepository
) {

    companion object {
        private const val VISIT_DISTANCE_LIMIT_METERS = 100.0
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val DEFAULT_GROUP_NAME = "기본"
    }

    @Transactional(readOnly = true)
    fun getRestaurants(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        pageable: Pageable
    ): Page<RestaurantListResponse> {
        val page = restaurantRepository.findByLocationBoundsWithFilters(
            minLat, maxLat, minLng, maxLng, priceRange, placeCategoryId, pageable
        )
        val likeCountMap = batchLikeCounts(page.content)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                likeCount = likeCountMap[restaurant.id] ?: 0L,
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id]
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
        pageable: Pageable
    ): Page<RestaurantListResponse> {
        val effectiveSearch = search?.takeIf { it.isNotBlank() }
        val effectiveTags = tags?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

        val page = if (sort == "visits") {
            restaurantRepository.findWithFiltersSortByVisits(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
        } else {
            restaurantRepository.findWithFilters(placeCategoryId, effectiveSearch, priceRange, effectiveTags, pageable)
        }

        val likeCountMap = batchLikeCounts(page.content)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                likeCount = likeCountMap[restaurant.id] ?: 0L,
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id]
            )
        }
    }

    @Transactional(readOnly = true)
    fun getRestaurantsByMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, priceRange: PriceRange?, pageable: Pageable
    ): Page<RestaurantListResponse> {
        if (memberIds.isEmpty()) return Page.empty()
        val page = if (priceRange != null) {
            restaurantRepository.findByLocationBoundsAndMembersAndPriceRange(minLat, maxLat, minLng, maxLng, memberIds, priceRange, pageable)
        } else {
            restaurantRepository.findByLocationBoundsAndMembers(minLat, maxLat, minLng, maxLng, memberIds, pageable)
        }
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                likeCount = 0L,
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id]
            )
        }
    }

    private fun batchLikeCounts(restaurants: List<Restaurant>): Map<Long, Long> {
        if (restaurants.isEmpty()) return emptyMap()
        return restaurantRepository.countLikesByRestaurants(restaurants)
            .associate { row -> (row[0] as Long) to (row[1] as Long) }
    }

    private fun batchVisitCounts(restaurants: List<Restaurant>): Map<Long, Long> {
        if (restaurants.isEmpty()) return emptyMap()
        return restaurantRepository.countVisitsByRestaurants(restaurants)
            .associate { row -> (row[0] as Long) to (row[1] as Long) }
    }

    // 이번 주 월요일~일요일 시간 범위 계산 (한국 시간 기준)
    private fun getWeekRange(): Pair<LocalDateTime, LocalDateTime> {
        val koreaZone = ZoneId.of("Asia/Seoul")
        val today = LocalDate.now(koreaZone)
        val monday = today.with(DayOfWeek.MONDAY)
        val nextMonday = monday.plusWeeks(1)
        return Pair(monday.atStartOfDay(), nextMonday.atStartOfDay())
    }

    // 모든 식당의 주간 방문왕을 배치로 조회
    private fun batchWeeklyChampions(restaurants: List<Restaurant>): Map<Long, String> {
        if (restaurants.isEmpty()) return emptyMap()
        val (weekStart, weekEnd) = getWeekRange()
        val results = visitRepository.findAllWeeklyChampions(weekStart, weekEnd)

        // 식당별로 가장 많이 방문한 유저의 닉네임 추출 (첫 번째가 최다 방문자)
        val championMap = mutableMapOf<Long, String>()
        for (row in results) {
            val restaurantId = row[0] as Long
            val nickname = row[1] as String
            // 첫 번째로 나오는 것이 최다 방문자 (ORDER BY cnt DESC)
            if (!championMap.containsKey(restaurantId)) {
                championMap[restaurantId] = nickname
            }
        }
        return championMap
    }

    @Transactional(readOnly = true)
    fun getRestaurantDetail(id: Long, userId: Long?): RestaurantDetailResponse {
        val restaurant = restaurantRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $id") }

        val likeCount = likeRepository.countDistinctUsersByRestaurant(restaurant)
        val visitCount = visitRepository.countByRestaurant(restaurant)
        val commentCount = commentRepository.countByRestaurantAndIsDeletedFalse(restaurant)

        val user = userId?.let { userRepository.findById(it).orElse(null) }
        val today = LocalDate.now()
        val isLiked = user != null &&
            likeRepository.findByLikeGroup_UserAndRestaurant(user, restaurant).isNotEmpty()
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
            likeCount = likeCount,
            visitCount = visitCount,
            commentCount = commentCount,
            isLiked = isLiked,
            isVisited = isVisited,
            priceRange = restaurant.priceRange?.name,
            placeCategoryId = restaurant.placeCategoryId,
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

    private fun createRestaurantFromQuickVisit(request: QuickVisitRequest, user: User): Restaurant =
        restaurantRepository.save(
            Restaurant(
                name = request.name,
                lat = request.lat,
                lng = request.lng,
                naverPlaceId = request.naverPlaceId,
                category = request.category,
                priceRange = request.priceRange,
                placeCategoryId = request.placeCategoryId,
                suggestedBy = user
            )
        )

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

        val restaurant = Restaurant(
            name = request.name,
            lat = request.lat,
            lng = request.lng,
            naverPlaceId = request.naverPlaceId,
            category = request.category,
            description = request.description,
            thumbnailImage = request.thumbnailImage,
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
        val likeCountMap = batchLikeCounts(page.content)
        val visitCountMap = batchVisitCounts(page.content)
        val weeklyChampionMap = batchWeeklyChampions(page.content)
        return page.map { restaurant ->
            restaurant.toListResponse(
                likeCount = likeCountMap[restaurant.id] ?: 0L,
                visitCount = visitCountMap[restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[restaurant.id]
            )
        }
    }

    // --- 컬렉션 기능 ---

    private fun ensureDefaultGroup(user: User): LikeGroup {
        return likeGroupRepository.findByUserAndName(user, DEFAULT_GROUP_NAME)
            .orElseGet { likeGroupRepository.save(LikeGroup(user = user, name = DEFAULT_GROUP_NAME)) }
    }

    @Transactional
    fun getCollections(userId: Long, restaurantId: Long?): List<LikeGroupResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        ensureDefaultGroup(user)
        val groups = likeGroupRepository.findByUser(user)

        val restaurant = restaurantId?.let {
            restaurantRepository.findById(it).orElse(null)
        }

        return groups.map { group ->
            val hasRestaurant = if (restaurant != null) {
                group.likes.any { it.restaurant.id == restaurant.id }
            } else false

            LikeGroupResponse(
                id = group.id,
                name = group.name,
                restaurantCount = group.likes.size.toLong(),
                hasRestaurant = hasRestaurant
            )
        }
    }

    @Transactional
    fun createCollection(userId: Long, name: String): LikeGroupResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        if (likeGroupRepository.existsByUserAndName(user, name)) {
            throw DuplicateResourceException("이미 같은 이름의 컬렉션이 있습니다")
        }

        val group = likeGroupRepository.save(LikeGroup(user = user, name = name))
        return LikeGroupResponse(
            id = group.id,
            name = group.name,
            restaurantCount = 0L,
            hasRestaurant = false
        )
    }

    @Transactional
    fun updateRestaurantCollections(
        restaurantId: Long,
        userId: Long,
        groupIds: List<Long>
    ): CollectionToggleResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val existingLikes = likeRepository.findByLikeGroup_UserAndRestaurant(user, restaurant)
        val existingGroupIds = existingLikes.map { it.likeGroup.id }.toSet()
        val targetGroupIds = groupIds.toSet()

        // 제거: targetGroupIds에 없는 기존 좋아요
        existingLikes.filter { it.likeGroup.id !in targetGroupIds }.forEach {
            likeRepository.delete(it)
        }

        // 추가: 기존에 없는 targetGroupIds
        val userGroups = likeGroupRepository.findByUser(user).associateBy { it.id }
        targetGroupIds.filter { it !in existingGroupIds }.forEach { groupId ->
            val group = userGroups[groupId]
                ?: throw ResourceNotFoundException("Collection not found: $groupId")
            likeRepository.save(Like(restaurant = restaurant, likeGroup = group))
        }

        val likeCount = likeRepository.countDistinctUsersByRestaurant(restaurant)
        val isLiked = targetGroupIds.isNotEmpty()

        return CollectionToggleResponse(isLiked = isLiked, likeCount = likeCount)
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
    fun getPlaceStats(naverPlaceId: String, userId: Long? = null): PlaceStatsResponse {
        val restaurant = restaurantRepository.findByNaverPlaceId(naverPlaceId)
            ?: throw ResourceNotFoundException("Place not found: $naverPlaceId")

        val visitCount = visitRepository.countByRestaurant(restaurant)
        val avgRating = visitRepository.findAvgRatingByRestaurant(restaurant)
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
            }
        )
    }

    @Transactional(readOnly = true)
    fun getCollectionRestaurants(userId: Long, groupId: Long, pageable: Pageable): Page<RestaurantListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }
        val group = likeGroupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Collection not found: $groupId") }

        if (group.user.id != user.id) {
            throw IllegalArgumentException("본인의 컬렉션만 조회할 수 있습니다")
        }

        val page = likeRepository.findByLikeGroup(group, pageable)
        val restaurants = page.content.map { it.restaurant }
        val likeCountMap = batchLikeCounts(restaurants)
        val visitCountMap = batchVisitCounts(restaurants)
        val weeklyChampionMap = batchWeeklyChampions(restaurants)

        return page.map { like ->
            like.restaurant.toListResponse(
                likeCount = likeCountMap[like.restaurant.id] ?: 0L,
                visitCount = visitCountMap[like.restaurant.id] ?: 0L,
                weeklyChampion = weeklyChampionMap[like.restaurant.id]
            )
        }
    }
}
