package com.mindbridge.dongnematjib.service

import com.mindbridge.dongnematjib.domain.restaurant.*
import com.mindbridge.dongnematjib.domain.user.User
import com.mindbridge.dongnematjib.dto.*
import com.mindbridge.dongnematjib.exception.ResourceNotFoundException
import com.mindbridge.dongnematjib.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val commentRepository: CommentRepository
) {

    @Transactional(readOnly = true)
    fun getRestaurants(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        pageable: Pageable
    ): Page<RestaurantListResponse> {
        return restaurantRepository.findByStatusAndLocationBounds(
            RestaurantStatus.APPROVED,
            minLat, maxLat, minLng, maxLng,
            pageable
        ).map { restaurant ->
            val likeCount = likeRepository.countByRestaurant(restaurant)
            restaurant.toListResponse(likeCount)
        }
    }

    @Transactional(readOnly = true)
    fun getRestaurantDetail(id: Long, userId: Long?): RestaurantDetailResponse {
        val restaurant = restaurantRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $id") }

        val likeCount = likeRepository.countByRestaurant(restaurant)
        val commentCount = commentRepository.countByRestaurantAndIsDeletedFalse(restaurant)
        
        val isLiked = userId?.let { uid ->
            userRepository.findById(uid).map { user ->
                likeRepository.existsByRestaurantAndUser(restaurant, user)
            }.orElse(false)
        } ?: false

        val isBookmarked = userId?.let { uid ->
            userRepository.findById(uid).map { user ->
                bookmarkRepository.existsByRestaurantAndUser(restaurant, user)
            }.orElse(false)
        } ?: false

        return RestaurantDetailResponse(
            id = restaurant.id,
            name = restaurant.name,
            address = restaurant.address,
            lat = restaurant.lat,
            lng = restaurant.lng,
            naverPlaceId = restaurant.naverPlaceId,
            category = restaurant.category,
            description = restaurant.description,
            thumbnailImage = restaurant.thumbnailImage,
            images = restaurant.images.sortedBy { it.displayOrder }.map { it.imageUrl },
            status = restaurant.status,
            suggestedBy = UserSummary(
                id = restaurant.suggestedBy.id,
                nickname = restaurant.suggestedBy.nickname,
                profileImage = restaurant.suggestedBy.profileImage
            ),
            likeCount = likeCount,
            commentCount = commentCount,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            createdAt = restaurant.createdAt,
            updatedAt = restaurant.updatedAt
        )
    }

    @Transactional
    fun createRestaurant(userId: Long, request: CreateRestaurantRequest): RestaurantDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        // 중복 체크 (네이버 장소 ID 기준)
        if (request.naverPlaceId != null && restaurantRepository.existsByNaverPlaceId(request.naverPlaceId)) {
            throw IllegalArgumentException("이미 등록된 장소입니다")
        }

        val restaurant = Restaurant(
            name = request.name,
            address = request.address,
            lat = request.lat,
            lng = request.lng,
            naverPlaceId = request.naverPlaceId,
            category = request.category,
            description = request.description,
            thumbnailImage = request.thumbnailImage,
            suggestedBy = user
        )

        // 이미지 추가
        request.imageUrls.forEachIndexed { index, url ->
            restaurant.images.add(
                RestaurantImage(
                    restaurant = restaurant,
                    imageUrl = url,
                    displayOrder = index
                )
            )
        }

        val saved = restaurantRepository.save(restaurant)
        return getRestaurantDetail(saved.id, userId)
    }

    @Transactional(readOnly = true)
    fun getMyRestaurants(userId: Long, pageable: Pageable): Page<RestaurantListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        return restaurantRepository.findBySuggestedBy(user, pageable).map { restaurant ->
            val likeCount = likeRepository.countByRestaurant(restaurant)
            restaurant.toListResponse(likeCount)
        }
    }

    @Transactional
    fun toggleLike(restaurantId: Long, userId: Long): Boolean {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val existingLike = likeRepository.findByRestaurantAndUser(restaurant, user)
        
        return if (existingLike.isPresent) {
            likeRepository.delete(existingLike.get())
            false
        } else {
            likeRepository.save(Like(restaurant = restaurant, user = user))
            true
        }
    }

    @Transactional
    fun toggleBookmark(restaurantId: Long, userId: Long): Boolean {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val existingBookmark = bookmarkRepository.findByRestaurantAndUser(restaurant, user)
        
        return if (existingBookmark.isPresent) {
            bookmarkRepository.delete(existingBookmark.get())
            false
        } else {
            bookmarkRepository.save(Bookmark(restaurant = restaurant, user = user))
            true
        }
    }

    @Transactional(readOnly = true)
    fun getBookmarkedRestaurants(userId: Long, pageable: Pageable): Page<RestaurantListResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        return bookmarkRepository.findByUser(user, pageable).map { bookmark ->
            val likeCount = likeRepository.countByRestaurant(bookmark.restaurant)
            bookmark.restaurant.toListResponse(likeCount)
        }
    }
}
