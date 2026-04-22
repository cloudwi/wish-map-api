package com.mindbridge.wishmap.context.place.domain

import org.springframework.data.jpa.repository.JpaRepository

interface NaverCategoryMappingRepository : JpaRepository<NaverCategoryMapping, Long> {
    fun findByNaverTop(naverTop: String): NaverCategoryMapping?
}
