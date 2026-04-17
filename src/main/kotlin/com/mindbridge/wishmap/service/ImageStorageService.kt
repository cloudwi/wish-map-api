package com.mindbridge.wishmap.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.UUID

@Service
class ImageStorageService(
    @Value("\${supabase.url:}") private val supabaseUrl: String,
    @Value("\${supabase.key:}") private val supabaseKey: String,
    @Value("\${supabase.bucket:place-images}") private val bucket: String
) {

    private val log = org.slf4j.LoggerFactory.getLogger(ImageStorageService::class.java)
    private val webClient = WebClient.builder()
        .codecs { it.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) }
        .build()

    /**
     * Supabase Storage에 이미지 업로드 후 public URL 반환 (Mono)
     */
    private fun uploadAsync(file: MultipartFile): Mono<String> {
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val fileName = "reviews/${UUID.randomUUID()}.$ext"
        val bytes = file.bytes

        return webClient.post()
            .uri("$supabaseUrl/storage/v1/object/$bucket/$fileName")
            .header("Authorization", "Bearer $supabaseKey")
            .header("apikey", supabaseKey)
            .header("x-upsert", "true")
            .contentType(MediaType.parseMediaType(file.contentType ?: "image/jpeg"))
            .bodyValue(bytes)
            .retrieve()
            .toBodilessEntity()
            .map {
                val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
                log.info("이미지 업로드: fileName={}, size={}KB", fileName, bytes.size / 1024)
                publicUrl
            }
            .subscribeOn(Schedulers.boundedElastic())
    }

    /**
     * 동기 단건 업로드 (기존 호환)
     */
    fun upload(file: MultipartFile): String {
        return uploadAsync(file).block()!!
    }

    /**
     * 여러 이미지 병렬 업로드
     */
    fun uploadAll(files: List<MultipartFile>): List<String> {
        return Mono.zip(
            files.map { uploadAsync(it) }
        ) { results -> results.map { it as String }.toList() }
            .block() ?: emptyList()
    }
}
