package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.dto.CreateRestaurantRequest
import com.mindbridge.wishmap.dto.RestaurantDetailResponse
import com.mindbridge.wishmap.dto.RestaurantListResponse
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.RestaurantService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService
) {

    @GetMapping
    fun getRestaurants(
        @RequestParam minLat: Double,
        @RequestParam maxLat: Double,
        @RequestParam minLng: Double,
        @RequestParam maxLng: Double,
        @PageableDefault(size = 50) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val restaurants = restaurantService.getRestaurants(
            minLat, maxLat, minLng, maxLng, pageable
        )
        return ResponseEntity.ok(restaurants)
    }

    @GetMapping("/{id}")
    fun getRestaurantDetail(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<RestaurantDetailResponse> {
        val restaurant = restaurantService.getRestaurantDetail(id, user?.id)
        return ResponseEntity.ok(restaurant)
    }

    @PostMapping
    fun createRestaurant(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantDetailResponse> {
        val restaurant = restaurantService.createRestaurant(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant)
    }

    @GetMapping("/my")
    fun getMyRestaurants(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val restaurants = restaurantService.getMyRestaurants(user.id, pageable)
        return ResponseEntity.ok(restaurants)
    }

    @PostMapping("/{id}/like")
    fun toggleLike(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Map<String, Boolean>> {
        val isLiked = restaurantService.toggleLike(id, user.id)
        return ResponseEntity.ok(mapOf("liked" to isLiked))
    }

    @PostMapping("/{id}/bookmark")
    fun toggleBookmark(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Map<String, Boolean>> {
        val isBookmarked = restaurantService.toggleBookmark(id, user.id)
        return ResponseEntity.ok(mapOf("bookmarked" to isBookmarked))
    }

    @GetMapping("/bookmarks")
    fun getBookmarkedRestaurants(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val restaurants = restaurantService.getBookmarkedRestaurants(user.id, pageable)
        return ResponseEntity.ok(restaurants)
    }
}
