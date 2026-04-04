package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.domain.restaurant.PriceRange
import com.mindbridge.wishmap.dto.*
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
        @RequestParam(required = false) minLat: Double?,
        @RequestParam(required = false) maxLat: Double?,
        @RequestParam(required = false) minLng: Double?,
        @RequestParam(required = false) maxLng: Double?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) priceRange: String?,
        @RequestParam(required = false) placeCategoryId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val parsedPriceRange = priceRange?.let {
            try { PriceRange.valueOf(it) } catch (_: IllegalArgumentException) { null }
        }
        return if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
            ResponseEntity.ok(restaurantService.getRestaurants(minLat, maxLat, minLng, maxLng, parsedPriceRange, placeCategoryId, pageable))
        } else {
            ResponseEntity.ok(restaurantService.getRestaurantsWithFilters(category, search, sortBy, parsedPriceRange, pageable))
        }
    }

    @GetMapping("/{id}")
    fun getRestaurantDetail(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<RestaurantDetailResponse> =
        ResponseEntity.ok(restaurantService.getRestaurantDetail(id, user?.id))

    @PostMapping
    fun createRestaurant(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantDetailResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(user.id, request))

    @GetMapping("/my")
    fun getMyRestaurants(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> =
        ResponseEntity.ok(restaurantService.getMyRestaurants(user.id, pageable))

    @PostMapping("/{id}/visit")
    fun verifyVisit(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: VisitVerifyRequest
    ): ResponseEntity<Map<String, Boolean>> {
        restaurantService.verifyVisit(id, user.id, request)
        return ResponseEntity.ok(mapOf("visited" to true))
    }

    @PostMapping("/quick-visit")
    fun quickVisit(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: QuickVisitRequest
    ): ResponseEntity<QuickVisitResponse> =
        ResponseEntity.ok(restaurantService.quickVisit(user.id, request))

    @GetMapping("/place-stats")
    fun getPlaceStats(
        @RequestParam naverPlaceId: String,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<PlaceStatsResponse> =
        ResponseEntity.ok(restaurantService.getPlaceStats(naverPlaceId, user?.id))

    // --- 컬렉션 ---

    @GetMapping("/collections")
    fun getCollections(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam(required = false) restaurantId: Long?
    ): ResponseEntity<List<LikeGroupResponse>> =
        ResponseEntity.ok(restaurantService.getCollections(user.id, restaurantId))

    @PostMapping("/collections")
    fun createCollection(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: LikeGroupCreateRequest
    ): ResponseEntity<LikeGroupResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createCollection(user.id, request.name))

    @GetMapping("/collections/{groupId}/restaurants")
    fun getCollectionRestaurants(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable groupId: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> =
        ResponseEntity.ok(restaurantService.getCollectionRestaurants(user.id, groupId, pageable))

    @PutMapping("/{id}/collections")
    fun updateRestaurantCollections(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody request: CollectionToggleRequest
    ): ResponseEntity<CollectionToggleResponse> =
        ResponseEntity.ok(restaurantService.updateRestaurantCollections(id, user.id, request.groupIds))
}
