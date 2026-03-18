package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.dto.RefreshTokenRequest
import com.mindbridge.wishmap.dto.SocialLoginRequest
import com.mindbridge.wishmap.dto.TokenResponse
import com.mindbridge.wishmap.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
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

}
