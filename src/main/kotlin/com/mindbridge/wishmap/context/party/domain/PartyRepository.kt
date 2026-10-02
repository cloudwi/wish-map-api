package com.mindbridge.wishmap.context.party.domain

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PartyRepository : JpaRepository<Party, Long> {
    @Query("select p from Party p where p.status = 'OPEN' and p.startsAt > :now and (:query = '' or lower(p.title) like concat('%', :query, '%') or lower(p.venueName) like concat('%', :query, '%') or lower(p.venueAddress) like concat('%', :query, '%')) order by p.startsAt asc")
    fun findUpcoming(@Param("now") now: LocalDateTime, @Param("query") query: String, pageable: Pageable): List<Party>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Party p where p.id = :id")
    fun lockById(@Param("id") id: Long): Party?
}

interface PartyMemberRepository : JpaRepository<PartyMember, Long> {
    fun findByPartyIdAndUserId(partyId: Long, userId: Long): PartyMember?
    fun findByPartyIdOrderByCreatedAtAsc(partyId: Long): List<PartyMember>
    fun countByPartyIdAndStatus(partyId: Long, status: String): Long
}
