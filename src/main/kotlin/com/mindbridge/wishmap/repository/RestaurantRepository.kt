package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.PriceRange
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
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
    """)
    fun findByLocationBoundsWithFilters(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        priceRange: PriceRange?,
        placeCategoryId: Long?,
        pageable: Pageable
    ): Page<Restaurant>

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
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
    """)
    fun findByLocationBoundsAndMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, priceRange: PriceRange?, pageable: Pageable
    ): Page<Restaurant>

    // 같은 카테고리 + 근접 위치(bounds)로 기존 장소 찾기 (커스텀 장소 중복 방지)
    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.placeCategoryId = :placeCategoryId
        AND r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND r.naverPlaceId IS NULL
        ORDER BY r.createdAt DESC
    """)
    fun findNearbyCustomByCategory(
        placeCategoryId: Long,
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<Restaurant>

    // 필터 + 검색 + 태그 + 최신순 정렬 (list 탭용)
    @Query("""
        SELECT DISTINCT r FROM Restaurant r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        ORDER BY r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Restaurant r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
    """)
    fun findWithFilters(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        pageable: Pageable
    ): Page<Restaurant>

    // 필터 + 검색 + 태그 + 방문 수 정렬 (list 탭용)
    @Query("""
        SELECT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        GROUP BY r
        ORDER BY COUNT(DISTINCT v) DESC, r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Restaurant r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
    """)
    fun findWithFiltersSortByVisits(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        pageable: Pageable
    ): Page<Restaurant>

    // 필터 + 검색 + 태그 + 최근 방문 인증 순 (list 탭용)
    @Query("""
        SELECT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        GROUP BY r
        ORDER BY MAX(v.createdAt) DESC NULLS LAST, r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Restaurant r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
    """)
    fun findWithFiltersSortByRecentVisit(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        pageable: Pageable
    ): Page<Restaurant>
}
