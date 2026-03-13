package com.mindbridge.dongnematjib.controller

import com.mindbridge.dongnematjib.domain.user.AuthProvider
import com.mindbridge.dongnematjib.dto.RefreshTokenRequest
import com.mindbridge.dongnematjib.dto.SocialLoginRequest
import com.mindbridge.dongnematjib.dto.TokenResponse
import com.mindbridge.dongnematjib.service.AuthService
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

    @DeleteMapping("/logout")
    fun logout(): ResponseEntity<Void> {
        // JWT는 stateless이므로 서버 측 로그아웃 처리 불필요
        // 클라이언트에서 토큰 삭제
        // 필요시 Redis 등에 블랙리스트 추가 가능
        return ResponseEntity.noContent().build()
    }
}
