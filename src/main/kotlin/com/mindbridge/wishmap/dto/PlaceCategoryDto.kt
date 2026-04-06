package com.mindbridge.wishmap.dto

data class PlaceCategoryResponse(
    val id: Long,
    val name: String,
    val icon: String?,
    val hasPriceRange: Boolean,
    val customOnly: Boolean,
    val tagGroups: List<TagGroupResponse>
)

data class TagGroupResponse(
    val key: String,
    val tags: List<String>
)
