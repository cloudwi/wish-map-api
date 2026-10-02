package com.mindbridge.wishmap.context.identity.infrastructure

import com.mindbridge.wishmap.common.error.ServiceUnavailableException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.security.SecureRandom
import java.time.Instant
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class SmsSender(
    private val webClient: WebClient,
    @Value("\${sms.solapi.api-key:}") private val apiKey: String,
    @Value("\${sms.solapi.api-secret:}") private val apiSecret: String,
    @Value("\${sms.solapi.sender:}") private val sender: String
) {
    private val random = SecureRandom()

    fun sendCode(phone: String, code: String) {
        if (apiKey.isBlank() || apiSecret.isBlank() || !sender.matches(Regex("0[0-9]{8,10}"))) {
            throw ServiceUnavailableException("문자 인증 발송 설정이 아직 완료되지 않았습니다.")
        }
        val date = Instant.now().toString()
        val salt = ByteArray(16).also(random::nextBytes).let(HexFormat.of()::formatHex)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(apiSecret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val signature = HexFormat.of().formatHex(mac.doFinal((date + salt).toByteArray(Charsets.UTF_8)))
        val authorization = "HMAC-SHA256 apiKey=$apiKey, date=$date, salt=$salt, signature=$signature"
        try {
            val result = webClient.post().uri("https://api.solapi.com/messages/v4/send-many/detail")
                .header("Authorization", authorization)
                .bodyValue(mapOf("messages" to listOf(mapOf(
                    "to" to phone, "from" to sender, "text" to "[위시맵] 인증번호는 $code 입니다. (5분 유효)", "type" to "SMS"
                )), "strict" to true))
                .retrieve().bodyToMono(Map::class.java).block()
                ?: throw ServiceUnavailableException("인증 문자를 보내지 못했습니다.")
            if ((result["failedCount"] as? Number)?.toInt()?.let { it > 0 } == true ||
                (result["failedMessageList"] as? Collection<*>)?.isNotEmpty() == true) {
                throw ServiceUnavailableException("인증 문자를 보내지 못했습니다.")
            }
        } catch (e: ServiceUnavailableException) {
            throw e
        } catch (e: Exception) {
            throw ServiceUnavailableException("인증 문자 발송에 실패했습니다. 잠시 후 다시 시도해주세요.")
        }
    }
}
