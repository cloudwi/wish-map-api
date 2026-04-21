package com.mindbridge.wishmap.context.place.domain

import com.mindbridge.wishmap.context.place.domain.TrendTag
import org.springframework.data.jpa.repository.JpaRepository

interface TrendTagRepository : JpaRepository<TrendTag, Long> {
    fun findByActiveTrueOrderByPriorityAsc(): List<TrendTag>
}
