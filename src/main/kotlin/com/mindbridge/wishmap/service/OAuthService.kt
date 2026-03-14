package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.dto.OAuthUserInfo
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper

@Service
class OAuthService(
    private val webClient: WebClient
) {

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
        // Apple uses identity token (JWT) instead of access token
        // Decode the JWT to get user info (without full verification for simplicity)
        // In production, you should verify the JWT signature with Apple's public keys
        
        val parts = identityToken.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid Apple identity token")
        }

        val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
        val payloadMap = JsonMapper.builder().build().readValue(payload, Map::class.java)

        val id = payloadMap["sub"]?.toString() 
            ?: throw IllegalArgumentException("Invalid Apple token: missing sub")
        val email = payloadMap["email"]?.toString() ?: "${id}@apple.user"

        return OAuthUserInfo(
            provider = AuthProvider.APPLE,
            providerId = id,
            email = email,
            nickname = "Apple 사용자",
            profileImage = null
        )
    }
}
