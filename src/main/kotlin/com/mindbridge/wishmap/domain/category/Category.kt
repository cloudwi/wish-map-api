package com.mindbridge.wishmap.domain.category

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(
    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false)
    var priority: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true
) : BaseTimeEntity()
