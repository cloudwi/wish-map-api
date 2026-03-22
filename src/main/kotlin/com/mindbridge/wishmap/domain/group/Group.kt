package com.mindbridge.wishmap.domain.group

import com.mindbridge.wishmap.domain.common.BaseEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "groups")
class Group(
    @Column(nullable = false, length = 100)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    var leader: User,

    @Column(name = "base_lat")
    var baseLat: Double? = null,

    @Column(name = "base_lng")
    var baseLng: Double? = null,

    @Column(name = "base_address", length = 255)
    var baseAddress: String? = null,

    @Column(name = "base_radius")
    var baseRadius: Int? = null,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<GroupMember> = mutableListOf()
) : BaseEntity()
