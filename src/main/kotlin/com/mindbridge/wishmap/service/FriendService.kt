package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.user.Friend
import com.mindbridge.wishmap.domain.user.FriendStatus
import com.mindbridge.wishmap.dto.FriendResponse
import com.mindbridge.wishmap.dto.FriendUserInfo
import com.mindbridge.wishmap.dto.UserSearchResult
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.FriendRepository
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FriendService(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) {

    fun searchUsers(query: String, currentUserId: Long): List<UserSearchResult> {
        if (query.length < 2) return emptyList()

        val users = userRepository.findByNicknameContainingIgnoreCase(query)
            .filter { it.id != currentUserId }
            .take(20)

        return users.map { user ->
            val relation = friendRepository.findBetween(currentUserId, user.id)
            UserSearchResult(
                id = user.id,
                nickname = user.nickname,
                profileImage = user.profileImage,
                friendStatus = relation?.status,
                friendId = relation?.id
            )
        }
    }

    @Transactional
    fun sendRequest(requesterId: Long, receiverId: Long): FriendResponse {
        require(requesterId != receiverId) { "자기 자신에게 친구 요청을 보낼 수 없습니다" }

        val existing = friendRepository.findBetween(requesterId, receiverId)
        require(existing == null) { "이미 친구 요청이 존재합니다" }

        val requester = userRepository.findById(requesterId).orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }
        val receiver = userRepository.findById(receiverId).orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }

        val friend = friendRepository.save(Friend(requester = requester, receiver = receiver))
        return friend.toResponse(currentUserId = requesterId)
    }

    @Transactional
    fun respondToRequest(friendId: Long, userId: Long, accept: Boolean): FriendResponse {
        val friend = friendRepository.findById(friendId).orElseThrow { ResourceNotFoundException("친구 요청을 찾을 수 없습니다") }
        require(friend.receiver.id == userId) { "권한이 없습니다" }
        require(friend.status == FriendStatus.PENDING) { "이미 처리된 요청입니다" }

        friend.status = if (accept) FriendStatus.ACCEPTED else FriendStatus.REJECTED
        return friend.toResponse(currentUserId = userId)
    }

    @Transactional
    fun removeFriend(friendId: Long, userId: Long) {
        val friend = friendRepository.findById(friendId).orElseThrow { ResourceNotFoundException("친구 관계를 찾을 수 없습니다") }
        require(friend.requester.id == userId || friend.receiver.id == userId) { "권한이 없습니다" }
        friendRepository.delete(friend)
    }

    fun getFriends(userId: Long): List<FriendResponse> =
        friendRepository.findByUserIdAndStatus(userId, FriendStatus.ACCEPTED)
            .map { it.toResponse(currentUserId = userId) }

    fun getPendingRequests(userId: Long): List<FriendResponse> =
        friendRepository.findPendingRequestsTo(userId)
            .map { it.toResponse(currentUserId = userId) }

    private fun Friend.toResponse(currentUserId: Long): FriendResponse {
        val isRequester = requester.id == currentUserId
        val friendUser = if (isRequester) receiver else requester
        return FriendResponse(
            id = id,
            user = FriendUserInfo(friendUser.id, friendUser.nickname, friendUser.profileImage),
            status = status,
            isRequester = isRequester,
            createdAt = createdAt
        )
    }
}
