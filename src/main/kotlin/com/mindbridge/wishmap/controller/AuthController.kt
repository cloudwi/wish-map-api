package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/{provider}")
    fun socialLogin(
        @PathVariable provider: String,
        @Valid @RequestBody request: SocialLoginRequest
    ): ResponseEntity<TokenResponse> {
        val authProvider = try {
            AuthProvider.valueOf(provider.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val response = authService.socialLogin(authProvider, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<TokenResponse> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/me/nickname")
    fun updateNickname(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: UpdateNicknameRequest
    ): ResponseEntity<UserResponse> =
        ResponseEntity.ok(authService.updateNickname(user.id, request.nickname.trim()))

    @PatchMapping("/me/push-token")
    fun updatePushToken(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody request: UpdatePushTokenRequest
    ): ResponseEntity<Void> {
        authService.updatePushToken(user.id, request.pushToken)
        return ResponseEntity.ok().build()
    }
}
