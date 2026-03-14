package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.repository.CategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class CategoryResponse(
    val id: Long,
    val name: String,
    val priority: Int
)

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryRepository: CategoryRepository
) {

    @GetMapping
    fun getCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = categoryRepository.findByActiveTrueOrderByPriorityAsc()
            .map { CategoryResponse(it.id, it.name, it.priority) }
        return ResponseEntity.ok(categories)
    }
}
