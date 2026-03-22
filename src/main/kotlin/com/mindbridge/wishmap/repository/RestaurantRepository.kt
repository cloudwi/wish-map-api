package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RestaurantRepository : JpaRepository<Restaurant, Long> {

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
    """)
    fun findByLocationBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        pageable: Pageable
    ): Page<Restaurant>

    @Query("""
        SELECT r.id, COUNT(DISTINCT l.likeGroup.user.id) FROM Restaurant r
        LEFT JOIN Like l ON l.restaurant = r
        WHERE r IN :restaurants
        GROUP BY r.id
    """)
    fun countLikesByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>

    fun findBySuggestedBy(user: User, pageable: Pageable): Page<Restaurant>

    fun existsByNaverPlaceId(naverPlaceId: String): Boolean
    fun findByNaverPlaceId(naverPlaceId: String): Restaurant?

    @Query("""
        SELECT r.id, COUNT(v) FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        WHERE r IN :restaurants
        GROUP BY r.id
    """)
    fun countVisitsByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>

    @Query("""
        SELECT DISTINCT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (r.suggestedBy.id IN :memberIds OR v.user.id IN :memberIds)
    """)
    fun findByLocationBoundsAndMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, pageable: Pageable
    ): Page<Restaurant>

    // 필터 + 검색 + 최신순 정렬 (list 탭용)
    @Query("""
        SELECT r FROM Restaurant r
        WHERE (COALESCE(:category, '') = '' OR r.category LIKE CONCAT(CAST(:category AS string), '%'))
        AND (COALESCE(:search, '') = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(r) FROM Restaurant r
        WHERE (COALESCE(:category, '') = '' OR r.category LIKE CONCAT(CAST(:category AS string), '%'))
        AND (COALESCE(:search, '') = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    fun findWithFilters(
        category: String?,
        search: String?,
        pageable: Pageable
    ): Page<Restaurant>

    // 필터 + 검색 + 방문 수 정렬 (list 탭용)
    @Query("""
        SELECT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        WHERE (COALESCE(:category, '') = '' OR r.category LIKE CONCAT(CAST(:category AS string), '%'))
        AND (COALESCE(:search, '') = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        GROUP BY r
        ORDER BY COUNT(v) DESC, r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(r) FROM Restaurant r
        WHERE (COALESCE(:category, '') = '' OR r.category LIKE CONCAT(CAST(:category AS string), '%'))
        AND (COALESCE(:search, '') = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    fun findWithFiltersSortByVisits(
        category: String?,
        search: String?,
        pageable: Pageable
    ): Page<Restaurant>
}
