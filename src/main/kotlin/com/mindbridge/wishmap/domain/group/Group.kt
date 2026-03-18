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

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<GroupMember> = mutableListOf()
) : BaseEntity()
