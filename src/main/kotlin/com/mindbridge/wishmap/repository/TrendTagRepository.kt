package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.trendtag.TrendTag
import org.springframework.data.jpa.repository.JpaRepository

interface TrendTagRepository : JpaRepository<TrendTag, Long> {
    fun findByActiveTrueOrderByPriorityAsc(): List<TrendTag>
}
