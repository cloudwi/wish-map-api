package com.mindbridge.wishmap.context.moderation.domain

import com.mindbridge.wishmap.context.moderation.domain.AgreementType
import com.mindbridge.wishmap.context.moderation.domain.UserAgreement
import org.springframework.data.jpa.repository.JpaRepository

interface UserAgreementRepository : JpaRepository<UserAgreement, Long> {
    fun existsByUserIdAndAgreementType(userId: Long, agreementType: AgreementType): Boolean
    fun deleteAllByUserId(userId: Long)
}
