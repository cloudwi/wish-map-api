package com.mindbridge.wishmap.context.social.api.dto

import com.mindbridge.wishmap.context.social.domain.FriendStatus
import java.time.LocalDateTime

data class UserSearchResult(
    val id: Long,
    val nickname: String,
    val profileImage: String?,
    val friendStatus: FriendStatus?,  // null = 관계 없음
    val friendId: Long?               // 친구 레코드 id (요청 수락/거절용)
)

data class FriendResponse(
    val id: Long,
    val user: FriendUserInfo,
    val status: FriendStatus,
    val isRequester: Boolean,
    val createdAt: LocalDateTime
)

data class FriendUserInfo(
    val id: Long,
    val nickname: String,
    val profileImage: String?
)
