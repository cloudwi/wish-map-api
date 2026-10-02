package com.mindbridge.wishmap.context.identity.api

import com.mindbridge.wishmap.context.identity.api.dto.*

import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import com.mindbridge.wishmap.context.identity.application.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/phone/request")
    fun requestCode(@Valid @RequestBody request: RequestPhoneCodeRequest): ResponseEntity<Void> {
        authService.requestCode(request.phone)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/phone/verify")
    fun verifyCode(@Valid @RequestBody request: VerifyPhoneCodeRequest): TokenResponse =
        authService.phoneLogin(request.phone, request.code)

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

    @DeleteMapping("/me")
    fun deleteAccount(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Void> {
        authService.deleteAccount(user.id)
        return ResponseEntity.noContent().build()
    }
}
