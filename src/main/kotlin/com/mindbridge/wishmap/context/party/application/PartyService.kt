package com.mindbridge.wishmap.context.party.application

import com.mindbridge.wishmap.common.error.ForbiddenException
import com.mindbridge.wishmap.common.error.ResourceNotFoundException
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import com.mindbridge.wishmap.context.notification.application.NotificationService
import com.mindbridge.wishmap.context.notification.domain.NotificationType
import com.mindbridge.wishmap.context.party.domain.Party
import com.mindbridge.wishmap.context.party.domain.PartyMember
import com.mindbridge.wishmap.context.party.domain.PartyMemberRepository
import com.mindbridge.wishmap.context.party.domain.PartyRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class CreatePartyRequest(
    val title: String,
    val description: String = "",
    val category: String = "OTHER",
    val venueName: String,
    val venueAddress: String,
    val latitude: Double,
    val longitude: Double,
    val startsAt: OffsetDateTime,
    val capacity: Int
)

data class PartyMemberResponse(val id: Long, val userId: Long, val nickname: String, val status: String)
data class PartyResponse(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val venueName: String,
    val venueAddress: String,
    val latitude: Double,
    val longitude: Double,
    val startsAt: String,
    val capacity: Int,
    val participantCount: Int,
    val status: String,
    val hostId: Long?,
    val hostNickname: String,
    val members: List<PartyMemberResponse>
)

@Service
class PartyService(
    private val parties: PartyRepository,
    private val members: PartyMemberRepository,
    private val users: UserRepository,
    private val notifications: NotificationService
) {
    @Transactional(readOnly = true)
    fun list(limit: Int, query: String?): List<PartyResponse> = parties.findUpcoming(LocalDateTime.now(ZoneOffset.UTC),
        query?.trim()?.lowercase() ?: "", PageRequest.of(0, limit.coerceIn(1, 100)))
        .map { response(it, false) }

    @Transactional(readOnly = true)
    fun detail(id: Long, viewerId: Long?): PartyResponse = response(find(id), true, viewerId)

    @Transactional
    fun create(userId: Long, request: CreatePartyRequest): PartyResponse {
        val host = getUser(userId)
        val title = request.title.trim()
        val description = request.description.trim()
        val venueName = request.venueName.trim()
        val venueAddress = request.venueAddress.trim()
        require(title.length in 2..80) { "제목은 2~80자로 입력해주세요" }
        require(description.length <= 1000) { "설명은 1000자 이하여야 합니다" }
        require(request.category.matches(Regex("[A-Z_]{2,30}"))) { "활동 종류를 확인해주세요" }
        require(venueName.length in 2..120 && venueAddress.length in 2..255) { "장소 이름과 주소를 확인해주세요" }
        require(request.latitude.isFinite() && request.latitude in -90.0..90.0) { "위도를 확인해주세요" }
        require(request.longitude.isFinite() && request.longitude in -180.0..180.0) { "경도를 확인해주세요" }
        require(request.capacity in 2..30) { "인원은 2~30명이어야 합니다" }
        val startsAt = request.startsAt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        require(startsAt.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30))) { "시작 시간은 30분 이후여야 합니다" }
        require(startsAt.isBefore(LocalDateTime.now(ZoneOffset.UTC).plusMonths(6))) { "6개월 이내의 날짜를 선택해주세요" }
        return response(parties.save(Party(host, title, description, request.category, venueName, venueAddress,
            request.latitude, request.longitude, startsAt, request.capacity)), true, userId)
    }

    @Transactional
    fun join(userId: Long, id: Long): PartyResponse {
        val user = getUser(userId)
        val party = parties.lockById(id) ?: throw ResourceNotFoundException("파티를 찾을 수 없습니다")
        require(party.status == "OPEN" && party.startsAt.isAfter(LocalDateTime.now(ZoneOffset.UTC))) { "모집 중인 파티가 아닙니다" }
        require(party.host.id != userId) { "주최자는 이미 참가 중입니다" }
        require(1 + members.countByPartyIdAndStatus(id, "APPROVED") < party.capacity) { "모집 인원이 가득 찼습니다" }
        val previous = members.findByPartyIdAndUserId(id, userId)
        require(previous == null || previous.status == "WITHDRAWN" || previous.status == "REJECTED") { "이미 신청한 파티입니다" }
        if (previous == null) members.save(PartyMember(party, user)) else previous.status = "PENDING"
        notifications.createNotification(party.host.id, NotificationType.PARTY_JOIN_REQUESTED,
            "새 참가 신청", "${user.nickname}님이 '${party.title}' 파티에 신청했어요.", party.id)
        return response(party, true, userId)
    }

    @Transactional
    fun decide(hostId: Long, partyId: Long, memberId: Long, approve: Boolean): PartyResponse {
        val party = parties.lockById(partyId) ?: throw ResourceNotFoundException("파티를 찾을 수 없습니다")
        requireHost(party, hostId)
        require(party.status == "OPEN" && party.startsAt.isAfter(LocalDateTime.now(ZoneOffset.UTC))) { "종료된 파티입니다" }
        val member = members.findById(memberId).orElseThrow { ResourceNotFoundException("신청을 찾을 수 없습니다") }
        require(member.party.id == partyId && member.status == "PENDING") { "대기 중인 신청이 아닙니다" }
        if (approve) require(1 + members.countByPartyIdAndStatus(partyId, "APPROVED") < party.capacity) { "모집 인원이 가득 찼습니다" }
        member.status = if (approve) "APPROVED" else "REJECTED"
        notifications.createNotification(member.user.id, if (approve) NotificationType.PARTY_JOIN_APPROVED else NotificationType.PARTY_JOIN_REJECTED,
            if (approve) "참가 승인" else "참가 신청 결과",
            if (approve) "'${party.title}' 파티에 참가하게 되었어요." else "'${party.title}' 파티 신청이 거절되었어요.", party.id)
        return response(party, true, hostId)
    }

    @Transactional
    fun withdraw(userId: Long, partyId: Long): PartyResponse {
        val party = parties.lockById(partyId) ?: throw ResourceNotFoundException("파티를 찾을 수 없습니다")
        val member = members.findByPartyIdAndUserId(partyId, userId) ?: throw ResourceNotFoundException("신청을 찾을 수 없습니다")
        require(member.status == "PENDING" || member.status == "APPROVED") { "취소할 신청이 없습니다" }
        member.status = "WITHDRAWN"
        return response(party, true, userId)
    }

    @Transactional
    fun cancel(hostId: Long, partyId: Long): PartyResponse {
        val party = parties.lockById(partyId) ?: throw ResourceNotFoundException("파티를 찾을 수 없습니다")
        requireHost(party, hostId)
        require(party.status == "OPEN") { "이미 취소된 파티입니다" }
        party.status = "CANCELLED"
        members.findByPartyIdOrderByCreatedAtAsc(partyId).filter { it.status == "APPROVED" || it.status == "PENDING" }
            .forEach { notifications.createNotification(it.user.id, NotificationType.PARTY_CANCELLED,
                "파티 취소", "'${party.title}' 파티가 취소되었어요.", party.id) }
        return response(party, true, hostId)
    }

    private fun getUser(id: Long) = users.findById(id).orElseThrow { ForbiddenException("로그인이 필요합니다") }

    private fun requireHost(party: Party, userId: Long) {
        if (party.host.id != userId) throw ForbiddenException("주최자만 처리할 수 있습니다")
    }

    private fun find(id: Long) = parties.findById(id).orElseThrow { ResourceNotFoundException("파티를 찾을 수 없습니다") }

    private fun response(party: Party, includeMembers: Boolean, viewerId: Long? = null): PartyResponse {
        val accepted = members.countByPartyIdAndStatus(party.id, "APPROVED").toInt()
        val memberList = if (includeMembers) members.findByPartyIdOrderByCreatedAtAsc(party.id)
            .filter { it.status == "APPROVED" || viewerId == party.host.id || viewerId == it.user.id }
            .map {
            PartyMemberResponse(it.id, it.user.id, it.user.nickname, it.status)
        } else emptyList()
        return PartyResponse(party.id, party.title, party.description, party.category, party.venueName,
            party.venueAddress, party.latitude, party.longitude, party.startsAt.toString() + "Z", party.capacity,
            accepted + 1, party.status, party.host.id,
            party.host.nickname, memberList)
    }
}
