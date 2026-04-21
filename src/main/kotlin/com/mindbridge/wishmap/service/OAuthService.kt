package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.dto.OAuthUserInfo
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.util.*

@Service
class OAuthService(
    private val webClient: WebClient,
    @Value("\${oauth.google.client-id:}") private val googleClientId: String,
    @Value("\${oauth.google.allowed-audiences:}") private val googleAllowedAudiencesRaw: String,
    @Value("\${oauth.apple.client-id:}") private val appleClientId: String
) {

    private val log = LoggerFactory.getLogger(OAuthService::class.java)

    private val googleAllowedAudiences: Set<String> by lazy {
        (googleAllowedAudiencesRaw.split(",") + googleClientId)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    fun verifyTokenAndGetUserInfo(provider: AuthProvider, accessToken: String): OAuthUserInfo {
        return when (provider) {
            AuthProvider.KAKAO -> verifyKakaoToken(accessToken)
            AuthProvider.GOOGLE -> verifyGoogleToken(accessToken)
            AuthProvider.NAVER -> verifyNaverToken(accessToken)
            AuthProvider.APPLE -> verifyAppleToken(accessToken)
        }
    }

    private fun verifyKakaoToken(accessToken: String): OAuthUserInfo {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw IllegalArgumentException("Failed to get Kakao user info")

        val id = response["id"].toString()
        val kakaoAccount = response["kakao_account"] as? Map<*, *>
        val email = kakaoAccount?.get("email")?.toString() 
            ?: "${id}@kakao.user"
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        val nickname = profile?.get("nickname")?.toString() ?: "카카오 사용자"
        val profileImage = profile?.get("profile_image_url")?.toString()

        return OAuthUserInfo(
            provider = AuthProvider.KAKAO,
            providerId = id,
            email = email,
            nickname = nickname,
            profileImage = profileImage
        )
    }

    private fun verifyGoogleToken(token: String): OAuthUserInfo {
        // 네이티브 SDK는 idToken(JWT)을, 웹은 accessToken을 보낼 수 있음
        // idToken인 경우 tokeninfo 엔드포인트로 검증
        val response = if (token.contains(".")) {
            // JWT 형태 → idToken
            webClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token=$token")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw IllegalArgumentException("Failed to verify Google id token")
        } else {
            // accessToken
            webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw IllegalArgumentException("Failed to get Google user info")
        }

        // audience 검증 (idToken인 경우 aud 필드 존재)
        // iOS/Android/Web 각각 다른 client ID를 발급하는 Google Sign-In 특성 상
        // 여러 audience 허용. oauth.google.allowed-audiences (콤마 구분) + oauth.google.client-id 합집합.
        val aud = response["aud"]?.toString()
        if (googleAllowedAudiences.isNotEmpty() && aud != null && aud !in googleAllowedAudiences) {
            log.warn("Google token audience mismatch: received aud={}, allowed={}", aud, googleAllowedAudiences)
            throw IllegalArgumentException("Google token audience mismatch")
        }

        val id = response["sub"].toString()
        val email = response["email"]?.toString() ?: "${id}@google.user"
        val nickname = response["name"]?.toString() ?: "구글 사용자"
        val profileImage = response["picture"]?.toString()

        return OAuthUserInfo(
            provider = AuthProvider.GOOGLE,
            providerId = id,
            email = email,
            nickname = nickname,
            profileImage = profileImage
        )
    }

    private fun verifyNaverToken(accessToken: String): OAuthUserInfo {
        val response = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw IllegalArgumentException("Failed to get Naver user info")

        val responseData = response["response"] as? Map<*, *>
            ?: throw IllegalArgumentException("Invalid Naver response")

        val id = responseData["id"].toString()
        val email = responseData["email"]?.toString() ?: "${id}@naver.user"
        val nickname = responseData["nickname"]?.toString() 
            ?: responseData["name"]?.toString() 
            ?: "네이버 사용자"
        val profileImage = responseData["profile_image"]?.toString()

        return OAuthUserInfo(
            provider = AuthProvider.NAVER,
            providerId = id,
            email = email,
            nickname = nickname,
            profileImage = profileImage
        )
    }

    private fun verifyAppleToken(identityToken: String): OAuthUserInfo {
        val signedJWT = try {
            SignedJWT.parse(identityToken)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Apple identity token format", e)
        }

        // Apple JWKS에서 공개키 가져오기
        val jwkSet = JWKSet.load(URI("https://appleid.apple.com/auth/keys").toURL())
        val kid = signedJWT.header.keyID
            ?: throw IllegalArgumentException("Apple token missing kid header")

        val jwk = jwkSet.getKeyByKeyId(kid)
            ?: throw IllegalArgumentException("Apple public key not found for kid: $kid")

        // 서명 검증
        val verifier = RSASSAVerifier(jwk as RSAKey)
        if (!signedJWT.verify(verifier)) {
            throw IllegalArgumentException("Apple token signature verification failed")
        }

        val claims = signedJWT.jwtClaimsSet

        // issuer 검증
        if (claims.issuer != "https://appleid.apple.com") {
            throw IllegalArgumentException("Invalid Apple token issuer: ${claims.issuer}")
        }

        // audience 검증
        if (appleClientId.isNotBlank() && !claims.audience.contains(appleClientId)) {
            throw IllegalArgumentException("Apple token audience mismatch")
        }

        // 만료 검증
        if (claims.expirationTime?.before(Date()) == true) {
            throw IllegalArgumentException("Apple token has expired")
        }

        val id = claims.subject
            ?: throw IllegalArgumentException("Invalid Apple token: missing sub")
        val email = claims.getStringClaim("email") ?: "${id}@apple.user"

        return OAuthUserInfo(
            provider = AuthProvider.APPLE,
            providerId = id,
            email = email,
            nickname = "Apple 사용자",
            profileImage = null
        )
    }

    fun revokeAppleToken(refreshToken: String) {
        // TODO: .p8 키 파일 설정 후 실제 해지 구현
        // Apple의 https://appleid.apple.com/auth/revoke 엔드포인트 호출 필요
        // client_secret은 .p8 키로 생성한 JWT (ES256)
        log.warn("Apple 토큰 해지 미구현 - .p8 키 설정 필요. refreshToken 길이: {}", refreshToken.length)
    }
}
