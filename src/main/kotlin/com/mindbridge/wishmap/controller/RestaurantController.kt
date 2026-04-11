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
@RequestMapping("/api/v1")
class RestaurantController(
    private val restaurantService: RestaurantService
) {

    @GetMapping("/restaurants", "/places")
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
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) tag: String?, // TODO: 하위 호환 - 구버전 앱 지원, 추후 제거
        @RequestParam(required = false) userLat: Double?,
        @RequestParam(required = false) userLng: Double?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val parsedPriceRange = priceRange?.let {
            try { PriceRange.valueOf(it) } catch (_: IllegalArgumentException) { null }
        }
        // 하위 호환: 구버전 앱의 tag 파라미터를 tags로 병합
        val effectiveTags = tags ?: tag?.let { listOf(it) }
        return if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
            ResponseEntity.ok(restaurantService.getRestaurants(minLat, maxLat, minLng, maxLng, parsedPriceRange, placeCategoryId, pageable))
        } else {
            ResponseEntity.ok(restaurantService.getRestaurantsWithFilters(category, search, sortBy, parsedPriceRange, placeCategoryId, effectiveTags, pageable, userLat = userLat, userLng = userLng))
        }
    }

    @GetMapping("/restaurants/{id}", "/places/{id}")
    fun getRestaurantDetail(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<RestaurantDetailResponse> =
        ResponseEntity.ok(restaurantService.getRestaurantDetail(id, user?.id))

    @PostMapping("/restaurants", "/places")
    fun createRestaurant(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantDetailResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(user.id, request))

    @GetMapping("/restaurants/my", "/places/my")
    fun getMyRestaurants(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> =
        ResponseEntity.ok(restaurantService.getMyRestaurants(user.id, pageable))

    @PostMapping("/restaurants/{id}/visit", "/places/{id}/visit")
    fun verifyVisit(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: VisitVerifyRequest
    ): ResponseEntity<Map<String, Boolean>> {
        restaurantService.verifyVisit(id, user.id, request)
        return ResponseEntity.ok(mapOf("visited" to true))
    }

    @PostMapping("/restaurants/quick-visit", "/places/quick-visit")
    fun quickVisit(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: QuickVisitRequest
    ): ResponseEntity<QuickVisitResponse> =
        ResponseEntity.ok(restaurantService.quickVisit(user.id, request))

    @GetMapping("/restaurants/stats/weekly-top", "/places/stats/weekly-top")
    fun getWeeklyTop(): ResponseEntity<List<WeeklyTopRestaurant>> =
        ResponseEntity.ok(restaurantService.getWeeklyTopRestaurants())

    @GetMapping("/restaurants/stats/popular", "/places/stats/popular")
    fun getPopular(): ResponseEntity<List<PopularRestaurant>> =
        ResponseEntity.ok(restaurantService.getPopularRestaurants())

    @GetMapping("/restaurants/stats/category-summary", "/places/stats/category-summary")
    fun getCategorySummary(): ResponseEntity<List<CategorySummary>> =
        ResponseEntity.ok(restaurantService.getCategorySummary())

    @GetMapping("/restaurants/place-stats", "/places/place-stats")
    fun getPlaceStats(
        @RequestParam naverPlaceId: String,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<PlaceStatsResponse> =
        ResponseEntity.ok(restaurantService.getPlaceStats(naverPlaceId, user?.id))

}
