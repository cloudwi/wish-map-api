package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.restaurant.RestaurantStatus
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateRestaurantRequest(
    @field:NotBlank(message = "가게 이름은 필수입니다")
    @field:Size(max = 100, message = "가게 이름은 100자 이하여야 합니다")
    val name: String,

    @field:NotBlank(message = "주소는 필수입니다")
    @field:Size(max = 500, message = "주소는 500자 이하여야 합니다")
    val address: String,

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
    val address: String,
    val lat: Double,
    val lng: Double,
    val category: String?,
    val thumbnailImage: String?,
    val likeCount: Long,
    val status: RestaurantStatus
)

data class RestaurantDetailResponse(
    val id: Long,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val naverPlaceId: String?,
    val category: String?,
    val description: String?,
    val thumbnailImage: String?,
    val images: List<String>,
    val status: RestaurantStatus,
    val suggestedBy: UserSummary,
    val likeCount: Long,
    val commentCount: Long,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserSummary(
    val id: Long,
    val nickname: String,
    val profileImage: String?
)

fun Restaurant.toListResponse(likeCount: Long) = RestaurantListResponse(
    id = id,
    name = name,
    address = address,
    lat = lat,
    lng = lng,
    category = category,
    thumbnailImage = thumbnailImage,
    likeCount = likeCount,
    status = status
)
