package com.mindbridge.wishmap.context.place.domain

enum class PriceRange(val label: String) {
    UNDER_10K("1만원 이하"),
    RANGE_10K("1만원대"),
    RANGE_20K("2만원대"),
    RANGE_30K("3만원대"),
    OVER_30K("3만원 이상")
}
