package com.mindbridge.wishmap.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

@Service
class ImageStorageService(
    @Value("\${supabase.url:}") private val supabaseUrl: String,
    @Value("\${supabase.key:}") private val supabaseKey: String,
    @Value("\${supabase.bucket:restaurant-images}") private val bucket: String
) {

    private val log = org.slf4j.LoggerFactory.getLogger(ImageStorageService::class.java)
    private val webClient = WebClient.builder().build()

    /**
     * Supabase Storage에 이미지 업로드 후 public URL 반환
     */
    fun upload(file: MultipartFile): String {
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val fileName = "reviews/${UUID.randomUUID()}.$ext"

        webClient.post()
            .uri("$supabaseUrl/storage/v1/object/$bucket/$fileName")
            .header("Authorization", "Bearer $supabaseKey")
            .header("apikey", supabaseKey)
            .header("x-upsert", "true")
            .contentType(MediaType.parseMediaType(file.contentType ?: "image/jpeg"))
            .bodyValue(file.bytes)
            .retrieve()
            .toBodilessEntity()
            .block()

        val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
        log.info("이미지 업로드: fileName={}, size={}KB", fileName, file.size / 1024)
        return publicUrl
    }

    /**
     * 여러 이미지 업로드
     */
    fun uploadAll(files: List<MultipartFile>): List<String> {
        return files.map { upload(it) }
    }
}
