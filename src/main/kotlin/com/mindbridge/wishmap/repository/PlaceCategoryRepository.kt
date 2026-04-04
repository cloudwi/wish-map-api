package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.placecategory.PlaceCategory
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceCategoryRepository : JpaRepository<PlaceCategory, Long> {
    fun findByActiveTrueOrderByPriorityAsc(): List<PlaceCategory>
}
