package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.service.ImageStorageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/images")
class ImageController(
    private val imageStorageService: ImageStorageService
) {

    @PostMapping("/upload")
    fun uploadImages(
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<Map<String, List<String>>> {
        if (files.size > 5) {
            return ResponseEntity.badRequest().body(mapOf("urls" to emptyList()))
        }

        val urls = imageStorageService.uploadAll(files)
        return ResponseEntity.ok(mapOf("urls" to urls))
    }
}
