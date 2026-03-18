package com.mindbridge.wishmap.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@RestController
@RequestMapping("/api/v1/search")
class SearchController(
    private val webClient: WebClient,
    @Value("\${naver.search.client-id:}") private val clientId: String,
    @Value("\${naver.search.client-secret:}") private val clientSecret: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/places")
    fun searchPlaces(
        @RequestParam query: String,
        @RequestParam(defaultValue = "15") display: Int
    ): ResponseEntity<Any> {
        if (clientId.isBlank()) {
            logger.error("Naver Search API client-id is not configured")
            return ResponseEntity.status(503).body(mapOf("error" to "Search service is not available"))
        }

        val clampedDisplay = display.coerceIn(1, 15)
        logger.debug("Search places: query={}, clientId={}", query, clientId.take(4) + "***")

        val result = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", clampedDisplay)
                    .queryParam("sort", "comment")
                    .build()
            }
            .header("X-Naver-Client-Id", clientId)
            .header("X-Naver-Client-Secret", clientSecret)
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        return ResponseEntity.ok(result)
    }
}
