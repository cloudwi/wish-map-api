package com.mindbridge.wishmap.context.place.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class NaverSearchService(
    private val webClient: WebClient,
    @Value("\${naver.search.client-id:}") private val clientId: String,
    @Value("\${naver.search.client-secret:}") private val clientSecret: String
) {

    private val log = LoggerFactory.getLogger(NaverSearchService::class.java)

    /**
     * 네이버 이미지 검색 → HTTPS thumbnail URL 반환
     * 외부 CDN 핫링크 차단을 우회하기 위해 네이버 프록시 thumbnail 사용
     *
     * 매칭률을 높이기 위한 fallback 전략:
     * 1차: 전체 이름 (display=5 중 첫 유효 결과)
     * 2차: 앞 2 단어만 (긴 이름은 매칭 실패 가능성 ↑)
     * 3차: 호출처가 제공한 fallbackQuery (예: "브랜드명 지역")
     */
    fun searchThumbnail(query: String, fallbackQuery: String? = null): String? {
        if (clientId.isBlank()) return null

        searchFirstValid(query)?.let { return it }

        val shortQuery = query.split(" ").filter { it.isNotBlank() }.take(2).joinToString(" ")
        if (shortQuery.isNotBlank() && shortQuery != query) {
            searchFirstValid(shortQuery)?.let { return it }
        }

        if (!fallbackQuery.isNullOrBlank() && fallbackQuery != query && fallbackQuery != shortQuery) {
            searchFirstValid(fallbackQuery)?.let { return it }
        }

        return null
    }

    private fun searchFirstValid(query: String): String? {
        return try {
            val result = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/image")
                        .queryParam("query", query)
                        .queryParam("display", 5)
                        .queryParam("sort", "sim")
                        .build()
                }
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val items = result?.get("items") as? List<*> ?: return null
            items.asSequence()
                .mapNotNull { it as? Map<*, *> }
                .mapNotNull { it["thumbnail"]?.toString() }
                .filter { it.isNotBlank() }
                // thumbnail은 b150(저화질) → b400으로 교체하여 고화질 사용
                .map { it.replace("type=b150", "type=b400") }
                .firstOrNull()
        } catch (e: Exception) {
            log.debug("이미지 검색 실패: query={}", query)
            null
        }
    }
}
