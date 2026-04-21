package com.mindbridge.wishmap.context.place.domain

import com.mindbridge.wishmap.context.place.domain.PlaceCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlaceCategoryRepository : JpaRepository<PlaceCategory, Long> {
    @Query("SELECT DISTINCT c FROM PlaceCategory c LEFT JOIN FETCH c.tags WHERE c.active = true ORDER BY c.priority ASC")
    fun findByActiveTrueOrderByPriorityAsc(): List<PlaceCategory>

    // 태그 미사용 시 cartesian product 회피 (TrendTagController 등에서 name→id 매핑용)
    @Query("SELECT c FROM PlaceCategory c WHERE c.active = true ORDER BY c.priority ASC")
    fun findActiveBasic(): List<PlaceCategory>
}
