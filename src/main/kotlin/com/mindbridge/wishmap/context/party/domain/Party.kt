package com.mindbridge.wishmap.context.party.domain

import com.mindbridge.wishmap.common.time.BaseEntity
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "parties")
class Party(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    var host: User,
    @Column(nullable = false, length = 80)
    var title: String,
    @Column(nullable = false, length = 1000)
    var description: String,
    @Column(nullable = false, length = 30)
    var category: String,
    @Column(name = "venue_name", nullable = false, length = 120)
    var venueName: String,
    @Column(name = "venue_address", nullable = false, length = 255)
    var venueAddress: String,
    @Column(nullable = false)
    var latitude: Double,
    @Column(nullable = false)
    var longitude: Double,
    @Column(name = "starts_at", nullable = false)
    var startsAt: LocalDateTime,
    @Column(nullable = false)
    var capacity: Int,
    @Column(nullable = false, length = 20)
    var status: String = "OPEN"
) : BaseEntity()

@Entity
@Table(name = "party_members")
class PartyMember(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    val party: Party,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false, length = 20)
    var status: String = "PENDING"
) : com.mindbridge.wishmap.common.time.BaseTimeEntity()
