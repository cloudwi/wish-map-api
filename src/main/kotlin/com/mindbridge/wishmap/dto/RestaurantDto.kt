package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.restaurant.PriceRange
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateRestaurantRequest(
    @field:NotBlank(message = "가게 이름은 필수입니다")
    @field:Size(max = 100, message = "가게 이름은 100자 이하여야 합니다")
    val name: String,

    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다")
    @field:DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다")
    val lat: Double,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다")
    @field:DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다")
    val lng: Double,

    val naverPlaceId: String? = null,

    @field:Size(max = 50, message = "카테고리는 50자 이하여야 합니다")
    val category: String? = null,

    @field:Size(max = 2000, message = "설명은 2000자 이하여야 합니다")
    val description: String? = null,

    val thumbnailImage: String? = null,

    val imageUrls: List<String> = emptyList()
)

data class RestaurantListResponse(
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val naverPlaceId: String?,
    val category: String?,
    val thumbnailImage: String?,
    val likeCount: Long,
    val visitCount: Long,
    val weeklyChampion: String? = null,
    val priceRange: String
)

data class RestaurantDetailResponse(
    val id: Long,
    val name: String,
    val lat: Double,
    val lng: Double,
    val naverPlaceId: String?,
    val category: String?,
    val description: String?,
    val thumbnailImage: String?,
    val images: List<String>,
    val suggestedBy: UserSummary,
    val likeCount: Long,
    val visitCount: Long,
    val commentCount: Long,
    val isLiked: Boolean,
    val isVisited: Boolean,
    val priceRange: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserSummary(
    val id: Long,
    val nickname: String,
    val profileImage: String?
)

data class VisitVerifyRequest(
    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다")
    @field:DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다")
    val lat: Double,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다")
    @field:DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다")
    val lng: Double
)

data class LikeGroupResponse(
    val id: Long,
    val name: String,
    val restaurantCount: Long,
    val hasRestaurant: Boolean
)

data class LikeGroupCreateRequest(
    @field:NotBlank(message = "그룹 이름은 필수입니다")
    @field:Size(max = 100, message = "그룹 이름은 100자 이하여야 합니다")
    val name: String
)

data class CollectionToggleRequest(
    val groupIds: List<Long>
)

data class CollectionToggleResponse(
    val isLiked: Boolean,
    val likeCount: Long
)

data class QuickVisitRequest(
    @field:NotBlank(message = "가게 이름은 필수입니다")
    val name: String,

    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다")
    @field:DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다")
    val lat: Double,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다")
    @field:DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다")
    val lng: Double,

    val naverPlaceId: String? = null,
    val category: String? = null,

    @field:NotNull(message = "사용자 위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다")
    @field:DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다")
    val userLat: Double,

    @field:NotNull(message = "사용자 경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다")
    @field:DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다")
    val userLng: Double,

    @field:Size(max = 2000)
    val comment: String? = null,

    @field:Min(1) @field:Max(5)
    val rating: Int? = null,

    @field:NotNull(message = "가격대는 필수입니다")
    val priceRange: PriceRange,

    val imageUrls: List<String> = emptyList()
)

data class QuickVisitResponse(
    val restaurantId: Long,
    val visited: Boolean,
    val isNew: Boolean
)

data class ReviewSummary(
    val nickname: String,
    val profileImage: String?,
    val content: String,
    val createdAt: LocalDateTime
)

data class PlaceStatsResponse(
    val restaurantId: Long,
    val visitCount: Long,
    val avgRating: Double?,
    val visitedToday: Boolean,
    val priceRange: String,
    val recentReviews: List<ReviewSummary>
)

fun Restaurant.toListResponse(likeCount: Long, visitCount: Long, weeklyChampion: String? = null) = RestaurantListResponse(
    id = id,
    name = name,
    lat = lat,
    lng = lng,
    naverPlaceId = naverPlaceId,
    category = category,
    thumbnailImage = thumbnailImage,
    likeCount = likeCount,
    visitCount = visitCount,
    weeklyChampion = weeklyChampion,
    priceRange = priceRange.name
)
