package com.mindbridge.wishmap.context.moderation.api

import com.mindbridge.wishmap.context.moderation.api.dto.CreateReportRequest
import com.mindbridge.wishmap.context.moderation.api.dto.ReportResponse
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.context.moderation.application.ModerationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val moderationService: ModerationService
) {

    @PostMapping
    fun createReport(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateReportRequest
    ): ResponseEntity<ReportResponse> {
        val response = moderationService.createReport(user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
