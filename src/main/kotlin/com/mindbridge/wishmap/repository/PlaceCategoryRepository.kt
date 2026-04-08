package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.placecategory.PlaceCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlaceCategoryRepository : JpaRepository<PlaceCategory, Long> {
    @Query("SELECT DISTINCT c FROM PlaceCategory c LEFT JOIN FETCH c.tags WHERE c.active = true ORDER BY c.priority ASC")
    fun findByActiveTrueOrderByPriorityAsc(): List<PlaceCategory>
}
