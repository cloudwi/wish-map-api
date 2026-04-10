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
    val visitCount: Long,
    val weeklyChampion: String? = null,
    val priceRange: String?,
    val placeCategoryId: Long?,
    val lastVisitedAt: java.time.LocalDateTime? = null
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
    val visitCount: Long,
    val commentCount: Long,
    val isVisited: Boolean,
    val priceRange: String?,
    val placeCategoryId: Long?,
    val lastVisitedAt: LocalDateTime? = null,
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

    val tags: List<String> = emptyList(),

    @field:Min(1) @field:Max(5)
    val rating: Int? = null,

    val priceRange: PriceRange? = null,

    val placeCategoryId: Long? = null,

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
    val tags: List<String>,
    val createdAt: LocalDateTime
)

data class PlaceStatsResponse(
    val restaurantId: Long,
    val visitCount: Long,
    val avgRating: Double?,
    val visitedToday: Boolean,
    val priceRange: String?,
    val placeCategoryId: Long?,
    val recentReviews: List<ReviewSummary>,
    val lastVisitedAt: LocalDateTime? = null
)

data class WeeklyTopRestaurant(
    val id: Long,
    val name: String,
    val category: String?,
    val thumbnailImage: String?,
    val visitCount: Long,
    val placeCategoryId: Long?
)

data class PopularRestaurant(
    val id: Long,
    val name: String,
    val category: String?,
    val thumbnailImage: String?,
    val totalVisitCount: Long,
    val placeCategoryId: Long?
)

data class CategorySummary(
    val placeCategoryId: Long,
    val name: String,
    val restaurantCount: Long
)

fun Restaurant.toListResponse(
    visitCount: Long,
    weeklyChampion: String? = null,
    lastVisitedAt: java.time.LocalDateTime? = null
) = RestaurantListResponse(
    id = id,
    name = name,
    lat = lat,
    lng = lng,
    naverPlaceId = naverPlaceId,
    category = category,
    thumbnailImage = thumbnailImage,
    visitCount = visitCount,
    weeklyChampion = weeklyChampion,
    priceRange = priceRange?.name,
    placeCategoryId = placeCategoryId,
    lastVisitedAt = lastVisitedAt
)
