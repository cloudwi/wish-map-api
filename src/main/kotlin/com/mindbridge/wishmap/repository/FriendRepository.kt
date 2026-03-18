package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.user.Friend
import com.mindbridge.wishmap.domain.user.FriendStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRepository : JpaRepository<Friend, Long> {

    @Query("""
        SELECT f FROM Friend f
        JOIN FETCH f.requester
        JOIN FETCH f.receiver
        WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
          AND f.status = :status
    """)
    fun findByUserIdAndStatus(userId: Long, status: FriendStatus): List<Friend>

    @Query("""
        SELECT f FROM Friend f
        JOIN FETCH f.requester
        WHERE f.receiver.id = :userId AND f.status = 'PENDING'
    """)
    fun findPendingRequestsTo(userId: Long): List<Friend>

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.requester.id = :userId AND f.receiver.id = :otherId)
           OR (f.requester.id = :otherId AND f.receiver.id = :userId)
    """)
    fun findBetween(userId: Long, otherId: Long): Friend?

    fun existsByRequesterIdAndReceiverId(requesterId: Long, receiverId: Long): Boolean
}
