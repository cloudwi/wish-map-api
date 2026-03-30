package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.moderation.AgreementType
import com.mindbridge.wishmap.domain.moderation.UserAgreement
import org.springframework.data.jpa.repository.JpaRepository

interface UserAgreementRepository : JpaRepository<UserAgreement, Long> {
    fun existsByUserIdAndAgreementType(userId: Long, agreementType: AgreementType): Boolean
    fun deleteAllByUserId(userId: Long)
}
