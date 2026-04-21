package com.mindbridge.wishmap.context.place.api

import com.mindbridge.wishmap.context.place.api.dto.PlaceCategoryResponse
import com.mindbridge.wishmap.context.place.api.dto.TagGroupResponse
import com.mindbridge.wishmap.context.place.domain.PlaceCategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/place-categories")
class PlaceCategoryController(
    private val placeCategoryRepository: PlaceCategoryRepository
) {
    // 카테고리는 거의 변하지 않으므로 1시간 in-memory 캐시
    private var cachedResponse: List<PlaceCategoryResponse>? = null
    private var cacheTime: Long = 0
    private val CACHE_TTL = 1000L * 60 * 60 // 1시간

    @GetMapping
    fun getPlaceCategories(): ResponseEntity<List<PlaceCategoryResponse>> {
        val now = System.currentTimeMillis()
        cachedResponse?.let {
            if (now - cacheTime < CACHE_TTL) return ResponseEntity.ok(it)
        }

        val categories = placeCategoryRepository.findByActiveTrueOrderByPriorityAsc()
            .map { category ->
                val tagGroups = category.tags
                    .groupBy { it.tagGroup }
                    .map { (group, tags) ->
                        TagGroupResponse(
                            key = group,
                            tags = tags.sortedBy { it.priority }.map { it.tag }
                        )
                    }
                PlaceCategoryResponse(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    hasPriceRange = category.hasPriceRange,
                    customOnly = category.customOnly,
                    tagGroups = tagGroups
                )
            }

        cachedResponse = categories
        cacheTime = now
        return ResponseEntity.ok(categories)
    }
}
