package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.domain.place.PriceRange
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.PlaceService
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
class PlaceController(
    private val placeService: PlaceService
) {

    @GetMapping("/places")
    fun getPlaces(
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
    ): ResponseEntity<Page<PlaceListResponse>> {
        val parsedPriceRange = priceRange?.let {
            try { PriceRange.valueOf(it) } catch (_: IllegalArgumentException) { null }
        }
        // 하위 호환: 구버전 앱의 tag 파라미터를 tags로 병합
        val effectiveTags = tags ?: tag?.let { listOf(it) }
        return if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
            require(minLat in -90.0..90.0 && maxLat in -90.0..90.0) { "위도는 -90~90 범위여야 합니다" }
            require(minLng in -180.0..180.0 && maxLng in -180.0..180.0) { "경도는 -180~180 범위여야 합니다" }
            require(minLat <= maxLat) { "minLat은 maxLat 이하여야 합니다" }
            require(minLng <= maxLng) { "minLng은 maxLng 이하여야 합니다" }
            ResponseEntity.ok(placeService.getPlaces(minLat, maxLat, minLng, maxLng, parsedPriceRange, placeCategoryId, effectiveTags, pageable))
        } else {
            ResponseEntity.ok(placeService.getPlacesWithFilters(category, search, sortBy, parsedPriceRange, placeCategoryId, effectiveTags, pageable, userLat = userLat, userLng = userLng))
        }
    }

    @GetMapping("/places/{id}")
    fun getPlaceDetail(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<PlaceDetailResponse> =
        ResponseEntity.ok(placeService.getPlaceDetail(id, user?.id))

    @PostMapping("/places")
    fun createPlace(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreatePlaceRequest
    ): ResponseEntity<PlaceDetailResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(placeService.createPlace(user.id, request))

    @GetMapping("/places/my")
    fun getMyPlaces(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<PlaceListResponse>> =
        ResponseEntity.ok(placeService.getMyPlaces(user.id, pageable))

    @PostMapping("/places/{id}/visit")
    fun verifyVisit(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: VisitVerifyRequest
    ): ResponseEntity<Map<String, Boolean>> {
        placeService.verifyVisit(id, user.id, request)
        return ResponseEntity.ok(mapOf("visited" to true))
    }

    @PostMapping("/places/quick-visit")
    fun quickVisit(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: QuickVisitRequest
    ): ResponseEntity<QuickVisitResponse> =
        ResponseEntity.ok(placeService.quickVisit(user.id, request))

    @GetMapping("/places/stats/weekly-top")
    fun getWeeklyTop(): ResponseEntity<List<WeeklyTopPlace>> =
        ResponseEntity.ok(placeService.getWeeklyTopPlaces())

    @GetMapping("/places/stats/popular")
    fun getPopular(): ResponseEntity<List<PopularPlace>> =
        ResponseEntity.ok(placeService.getPopularPlaces())

    @GetMapping("/places/stats/category-summary")
    fun getCategorySummary(): ResponseEntity<List<CategorySummary>> =
        ResponseEntity.ok(placeService.getCategorySummary())

    @GetMapping("/places/place-stats")
    fun getPlaceStats(
        @RequestParam naverPlaceId: String,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<PlaceStatsResponse> =
        ResponseEntity.ok(placeService.getPlaceStats(naverPlaceId, user?.id))

}
