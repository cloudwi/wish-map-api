package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PushNotificationService(
    private val userRepository: UserRepository,
    private val webClient: WebClient
) {

    private val log = LoggerFactory.getLogger(PushNotificationService::class.java)

    fun sendPush(userId: Long, title: String, body: String, data: Map<String, String>? = null) {
        val user = userRepository.findById(userId).orElse(null) ?: return
        val token = user.pushToken ?: return

        sendToTokens(listOf(token), title, body, data)
    }

    fun sendPushToUsers(userIds: List<Long>, title: String, body: String, data: Map<String, String>? = null) {
        if (userIds.isEmpty()) return

        val users = userRepository.findByIdIn(userIds)
        val tokens = users.mapNotNull { it.pushToken }
        if (tokens.isEmpty()) return

        sendToTokens(tokens, title, body, data)
    }

    private fun sendToTokens(tokens: List<String>, title: String, body: String, data: Map<String, String>?) {
        val messages = tokens.map { token ->
            buildMap {
                put("to", token)
                put("title", title)
                put("body", body)
                put("sound", "default")
                if (data != null) {
                    put("data", data)
                }
            }
        }

        try {
            webClient.post()
                .uri("https://exp.host/--/api/v2/push/send")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(messages)
                .retrieve()
                .bodyToMono(String::class.java)
                .subscribe(
                    { response -> log.debug("Expo push response: {}", response) },
                    { error -> log.error("Failed to send push notification", error) }
                )
        } catch (e: Exception) {
            log.error("Failed to send push notification", e)
        }
    }
}
