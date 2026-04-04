package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.dto.PlaceCategoryResponse
import com.mindbridge.wishmap.dto.TagGroupResponse
import com.mindbridge.wishmap.repository.PlaceCategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/place-categories")
class PlaceCategoryController(
    private val placeCategoryRepository: PlaceCategoryRepository
) {

    @GetMapping
    fun getPlaceCategories(): ResponseEntity<List<PlaceCategoryResponse>> {
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
                    tagGroups = tagGroups
                )
            }
        return ResponseEntity.ok(categories)
    }
}
