package com.mindbridge.wishmap.context.place.api

import com.mindbridge.wishmap.context.place.api.dto.*

import com.mindbridge.wishmap.context.place.domain.TrendTagRepository
import com.mindbridge.wishmap.context.place.domain.PlaceCategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TrendTagResponse(
    val id: Long,
    val label: String,
    val placeCategoryId: Long? = null,
    val tags: List<String>? = null,
    val priceRange: String? = null,
)

@RestController
@RequestMapping("/api/v1/trend-tags")
class TrendTagController(
    private val trendTagRepository: TrendTagRepository,
    private val placeCategoryRepository: PlaceCategoryRepository,
) {

    @GetMapping
    fun getTrendTags(): ResponseEntity<List<TrendTagResponse>> {
        val categories = placeCategoryRepository.findActiveBasic()
        val categoryMap = categories.associate { it.name to it.id }

        val tags = trendTagRepository.findByActiveTrueOrderByPriorityAsc().map { t ->
            TrendTagResponse(
                id = t.id,
                label = t.label,
                placeCategoryId = t.categoryName?.let { categoryMap[it] },
                tags = t.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() },
                priceRange = t.priceRange,
            )
        }
        return ResponseEntity.ok(tags)
    }
}
