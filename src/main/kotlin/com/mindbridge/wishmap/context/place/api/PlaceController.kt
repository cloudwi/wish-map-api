package com.mindbridge.wishmap.context.place.api

import com.mindbridge.wishmap.context.place.api.dto.*

import com.mindbridge.wishmap.context.place.domain.PriceRange
import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import com.mindbridge.wishmap.context.place.application.PlaceService
import jakarta.validation.Valid
import org.springframework.data.domain.Slice
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
        // Keyset pagination 파라미터
        // - 기본 최신순: cursorCreatedAt + cursorId
        // - sortBy=visits: cursorVisitCount + cursorId
        @RequestParam(required = false) cursorCreatedAt: java.time.LocalDateTime?,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorVisitCount: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Slice<PlaceListResponse>> {
        val parsedPriceRange = priceRange?.let {
            try { PriceRange.valueOf(it) } catch (_: IllegalArgumentException) { null }
        }
        // 하위 호환: 구버전 앱의 tag 파라미터를 tags로 병합
        val effectiveTags = tags ?: tag?.let { listOf(it) }

        // bounds가 있으면 지도 기반 조회 (keyset 미적용, 공간 쿼리이므로 bounds 자체가 제한자)
        if (minLat != null && maxLat != null && minLng != null && maxLng != null) {
            require(minLat in -90.0..90.0 && maxLat in -90.0..90.0) { "위도는 -90~90 범위여야 합니다" }
            require(minLng in -180.0..180.0 && maxLng in -180.0..180.0) { "경도는 -180~180 범위여야 합니다" }
            require(minLat <= maxLat) { "minLat은 maxLat 이하여야 합니다" }
            require(minLng <= maxLng) { "minLng은 maxLng 이하여야 합니다" }
            return ResponseEntity.ok(placeService.getPlaces(minLat, maxLat, minLng, maxLng, parsedPriceRange, placeCategoryId, effectiveTags, pageable))
        }

        // 기본 최신순 cursor 경로
        if (sortBy == null && cursorCreatedAt != null && cursorId != null) {
            return ResponseEntity.ok(
                placeService.getPlacesByCursor(
                    placeCategoryId, search, parsedPriceRange, effectiveTags,
                    cursorCreatedAt, cursorId, pageable.pageSize
                )
            )
        }

        // sortBy=visits cursor 경로 (복합 커서: visitCount + id)
        if (sortBy == "visits" && cursorVisitCount != null && cursorId != null) {
            return ResponseEntity.ok(
                placeService.getPlacesByCursorSortByVisits(
                    placeCategoryId, search, parsedPriceRange, effectiveTags,
                    cursorVisitCount, cursorId, pageable.pageSize
                )
            )
        }

        return ResponseEntity.ok(
            placeService.getPlacesWithFilters(
                category, search, sortBy, parsedPriceRange, placeCategoryId, effectiveTags,
                pageable, userLat = userLat, userLng = userLng
            )
        )
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
        @RequestParam(required = false) cursorCreatedAt: java.time.LocalDateTime?,
        @RequestParam(required = false) cursorId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Slice<PlaceListResponse>> {
        val result = if (cursorCreatedAt != null && cursorId != null) {
            placeService.getMyPlacesByCursor(user.id, cursorCreatedAt, cursorId, pageable.pageSize)
        } else {
            placeService.getMyPlaces(user.id, pageable)
        }
        return ResponseEntity.ok(result)
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
