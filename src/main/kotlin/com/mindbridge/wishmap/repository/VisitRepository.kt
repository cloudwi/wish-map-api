package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.place.Place
import com.mindbridge.wishmap.domain.place.Visit
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface VisitRepository : JpaRepository<Visit, Long> {
    fun existsByPlaceAndUserAndCreatedAtBetween(
        place: Place,
        user: User,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean

    @Query("SELECT COUNT(v) > 0 FROM Visit v WHERE v.place.id = :placeId AND v.user.id = :userId AND v.createdAt BETWEEN :start AND :end")
    fun existsByPlaceIdAndUserIdAndCreatedAtBetween(
        placeId: Long,
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean

    fun countByPlace(place: Place): Long
    fun countByPlaceAndUser(place: Place, user: User): Long

    @Query("SELECT AVG(v.rating) FROM Visit v WHERE v.place = :place AND v.rating IS NOT NULL")
    fun findAvgRatingByPlace(place: Place): Double?

    // 지정된 식당들의 주간 방문왕 (배치 조회용)
    @Query("""
        SELECT v.place.id, v.user.nickname, COUNT(v) as cnt
        FROM Visit v
        WHERE v.place.id IN :placeIds
          AND v.createdAt >= :weekStart AND v.createdAt < :weekEnd
        GROUP BY v.place.id, v.user
        ORDER BY v.place.id, cnt DESC
    """)
    fun findWeeklyChampionsByPlaceIds(
        placeIds: List<Long>,
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): List<Array<Any>>

    // 특정 식당에서 유저별 방문 횟수 (배치 조회)
    @Query("""
        SELECT v.user.id, COUNT(v)
        FROM Visit v
        WHERE v.place = :place AND v.user IN :users
        GROUP BY v.user.id
    """)
    fun countByPlaceAndUsers(place: Place, users: List<User>): List<Array<Any>>

    @Query("""
        SELECT v.place.id, MAX(v.createdAt)
        FROM Visit v
        WHERE v.user = :user AND v.place.id IN :placeIds
        GROUP BY v.place.id
    """)
    fun findLastVisitDatesByUserAndPlaceIds(user: User, placeIds: List<Long>): List<Array<Any>>

    fun findFirstByPlaceOrderByCreatedAtDesc(place: Place): Visit?

    // 여러 식당의 마지막 방문일 (배치 조회)
    @Query("""
        SELECT v.place.id, MAX(v.createdAt)
        FROM Visit v
        WHERE v.place.id IN :placeIds
        GROUP BY v.place.id
    """)
    fun findLastVisitDatesByPlaceIds(placeIds: List<Long>): List<Array<Any>>

    fun deleteAllByUser(user: User)

    // 여러 식당의 최다 보고 가격대 (배치 조회)
    @Query("""
        SELECT v.place.id, v.priceRange, COUNT(v) as cnt
        FROM Visit v
        WHERE v.place IN :places AND v.priceRange IS NOT NULL
        GROUP BY v.place.id, v.priceRange
        ORDER BY v.place.id, cnt DESC
    """)
    fun findPriceRangesByPlaces(places: List<Place>): List<Array<Any>>
}
