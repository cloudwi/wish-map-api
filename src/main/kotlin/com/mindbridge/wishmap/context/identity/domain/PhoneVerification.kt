package com.mindbridge.wishmap.context.identity.domain

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

@Entity
@Table(name = "phone_verifications")
class PhoneVerification(
    @Id @Column(length = 11) val phone: String,
    @Column(name = "code_hash", length = 64) var codeHash: String? = null,
    @Column(name = "expires_at") var expiresAt: LocalDateTime? = null,
    @Column(nullable = false) var attempts: Int = 0,
    @Column(name = "sent_count", nullable = false) var sentCount: Int = 0,
    @Column(name = "window_started_at", nullable = false) var windowStartedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "last_sent_at") var lastSentAt: LocalDateTime? = null
)

interface PhoneVerificationRepository : JpaRepository<PhoneVerification, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from PhoneVerification v where v.phone = :phone")
    fun lockByPhone(@Param("phone") phone: String): PhoneVerification?

    @Modifying
    @Query("delete from PhoneVerification v where v.lastSentAt < :cutoff")
    fun deleteStale(@Param("cutoff") cutoff: LocalDateTime): Int
}
