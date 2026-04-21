package com.mindbridge.wishmap.context.moderation.api

import com.mindbridge.wishmap.context.moderation.domain.AgreementType
import com.mindbridge.wishmap.context.moderation.api.dto.AgreementResponse
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.context.moderation.application.ModerationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/agreements")
class AgreementController(
    private val moderationService: ModerationService
) {

    @PostMapping("/{type}")
    fun agree(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable type: String
    ): ResponseEntity<AgreementResponse> {
        val agreementType = try {
            AgreementType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
        moderationService.agreeToTerms(user.id, agreementType)
        return ResponseEntity.ok(AgreementResponse(agreed = true))
    }

    @GetMapping("/{type}")
    fun checkAgreement(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable type: String
    ): ResponseEntity<AgreementResponse> {
        val agreementType = try {
            AgreementType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
        val agreed = moderationService.hasAgreed(user.id, agreementType)
        return ResponseEntity.ok(AgreementResponse(agreed = agreed))
    }
}
