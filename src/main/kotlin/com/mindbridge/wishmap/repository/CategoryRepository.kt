package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.category.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByActiveTrueOrderByPriorityAsc(): List<Category>
}
