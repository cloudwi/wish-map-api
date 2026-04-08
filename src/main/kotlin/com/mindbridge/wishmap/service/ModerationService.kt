package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.moderation.*
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.DuplicateResourceException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.BlockedUserRepository
import com.mindbridge.wishmap.repository.ReportRepository
import com.mindbridge.wishmap.repository.UserAgreementRepository
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ModerationService(
    private val reportRepository: ReportRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userAgreementRepository: UserAgreementRepository,
    private val userRepository: UserRepository
) {

    private val log = org.slf4j.LoggerFactory.getLogger(ModerationService::class.java)

    @Transactional
    fun createReport(userId: Long, request: CreateReportRequest): ReportResponse {
        val reporter = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(userId, request.targetType, request.targetId)) {
            throw DuplicateResourceException("이미 신고한 콘텐츠입니다")
        }

        val report = Report(
            reporter = reporter,
            targetType = request.targetType,
            targetId = request.targetId,
            reason = request.reason,
            description = request.description
        )
        reportRepository.save(report)
        log.warn("신고 접수: reporterId={}, targetType={}, targetId={}, reason={}", userId, request.targetType, request.targetId, request.reason)

        return ReportResponse(
            id = report.id,
            targetType = report.targetType,
            targetId = report.targetId,
            reason = report.reason,
            description = report.description,
            createdAt = report.createdAt
        )
    }

    @Transactional
    fun blockUser(blockerId: Long, blockedId: Long): BlockedUserResponse {
        if (blockerId == blockedId) {
            throw IllegalArgumentException("자기 자신을 차단할 수 없습니다")
        }

        val blocker = userRepository.findById(blockerId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val blocked = userRepository.findById(blockedId)
            .orElseThrow { ResourceNotFoundException("차단 대상 사용자를 찾을 수 없습니다") }

        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw DuplicateResourceException("이미 차단한 사용자입니다")
        }

        val blockedUser = BlockedUser(blocker = blocker, blocked = blocked)
        blockedUserRepository.save(blockedUser)
        log.info("사용자 차단: blockerId={}, blockedId={}", blockerId, blockedId)

        return BlockedUserResponse(
            id = blockedUser.id,
            userId = blocked.id,
            nickname = blocked.nickname,
            profileImage = blocked.profileImage,
            blockedAt = blockedUser.createdAt
        )
    }

    @Transactional
    fun unblockUser(blockerId: Long, blockedId: Long) {
        val blockedUser = blockedUserRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
            ?: throw ResourceNotFoundException("차단 내역을 찾을 수 없습니다")
        blockedUserRepository.delete(blockedUser)
    }

    @Transactional(readOnly = true)
    fun getBlockedUsers(blockerId: Long): List<BlockedUserResponse> {
        return blockedUserRepository.findAllByBlockerId(blockerId).map {
            BlockedUserResponse(
                id = it.id,
                userId = it.blocked.id,
                nickname = it.blocked.nickname,
                profileImage = it.blocked.profileImage,
                blockedAt = it.createdAt
            )
        }
    }

    @Transactional(readOnly = true)
    fun getBlockedUserIds(userId: Long): List<Long> {
        return blockedUserRepository.findBlockedUserIds(userId)
    }

    @Transactional
    fun agreeToTerms(userId: Long, type: AgreementType) {
        if (userAgreementRepository.existsByUserIdAndAgreementType(userId, type)) {
            return
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        userAgreementRepository.save(UserAgreement(user = user, agreementType = type))
    }

    @Transactional(readOnly = true)
    fun hasAgreed(userId: Long, type: AgreementType): Boolean {
        return userAgreementRepository.existsByUserIdAndAgreementType(userId, type)
    }
}
