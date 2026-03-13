package com.mindbridge.dongnematjib.controller

import com.mindbridge.dongnematjib.dto.RestaurantListResponse
import com.mindbridge.dongnematjib.security.UserPrincipal
import com.mindbridge.dongnematjib.service.AdminService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("/restaurants/pending")
    fun getPendingRestaurants(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RestaurantListResponse>> {
        val restaurants = adminService.getPendingRestaurants(pageable)
        return ResponseEntity.ok(restaurants)
    }

    @PostMapping("/restaurants/{id}/approve")
    fun approveRestaurant(
        @PathVariable id: Long,
        @AuthenticationPrincipal admin: UserPrincipal
    ): ResponseEntity<Map<String, String>> {
        adminService.approveRestaurant(id, admin.id)
        return ResponseEntity.ok(mapOf("message" to "승인되었습니다"))
    }

    @PostMapping("/restaurants/{id}/reject")
    fun rejectRestaurant(
        @PathVariable id: Long,
        @AuthenticationPrincipal admin: UserPrincipal
    ): ResponseEntity<Map<String, String>> {
        adminService.rejectRestaurant(id, admin.id)
        return ResponseEntity.ok(mapOf("message" to "거절되었습니다"))
    }
}
