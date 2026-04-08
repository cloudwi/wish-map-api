package com.mindbridge.wishmap.service

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
     */
    fun searchThumbnail(query: String): String? {
        if (clientId.isBlank()) return null
        return try {
            val result = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/image")
                        .queryParam("query", query)
                        .queryParam("display", 1)
                        .queryParam("sort", "sim")
                        .build()
                }
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val item = (result?.get("items") as? List<*>)?.firstOrNull() as? Map<*, *>
            // thumbnail은 b150(저화질) → b400으로 교체하여 고화질 사용
            item?.get("thumbnail")?.toString()?.replace("type=b150", "type=b400")
        } catch (e: Exception) {
            log.debug("이미지 검색 실패: query={}", query)
            null
        }
    }
}
