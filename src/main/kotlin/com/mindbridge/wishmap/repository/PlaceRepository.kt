package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.place.PriceRange
import com.mindbridge.wishmap.domain.place.Place
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PlaceRepository : JpaRepository<Place, Long> {

    @Query("""
        SELECT r FROM Place r
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
    ): Page<Place>

    @Query("""
        SELECT DISTINCT r FROM Place r
        JOIN Comment c ON c.place = r AND c.isDeleted = false
        JOIN c.tags ct
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND ct.tag IN :tags
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Place r
        JOIN Comment c ON c.place = r AND c.isDeleted = false
        JOIN c.tags ct
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND ct.tag IN :tags
    """)
    fun findByLocationBoundsWithTags(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        priceRange: PriceRange?, placeCategoryId: Long?,
        tags: List<String>, pageable: Pageable
    ): Page<Place>

    fun findBySuggestedBy(user: User, pageable: Pageable): Page<Place>

    fun existsByNaverPlaceId(naverPlaceId: String): Boolean
    fun findByNaverPlaceId(naverPlaceId: String): Place?

    @Query("""
        SELECT r.id, COUNT(v) FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE r IN :places
        GROUP BY r.id
    """)
    fun countVisitsByPlaces(places: List<Place>): List<Array<Any>>

    @Query("""
        SELECT DISTINCT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (r.suggestedBy.id IN :memberIds OR v.user.id IN :memberIds)
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
    """)
    fun findByLocationBoundsAndMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, priceRange: PriceRange?, pageable: Pageable
    ): Page<Place>

    // 같은 카테고리 + 근접 위치(bounds)로 기존 장소 찾기 (커스텀 장소 중복 방지)
    @Query("""
        SELECT r FROM Place r
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
    ): List<Place>

    // 필터 + 검색 + 태그 + 최신순 정렬 (list 탭용)
    @Query("""
        SELECT DISTINCT r FROM Place r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        ORDER BY r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Place r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
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
    ): Page<Place>

    // 필터 + 검색 + 태그 + 방문 수 정렬 (list 탭용)
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        GROUP BY r
        ORDER BY COUNT(DISTINCT v) DESC, r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Place r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
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
    ): Page<Place>

    // 필터 + 검색 + 태그 + 최근 방문 인증 순 (list 탭용)
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
        LEFT JOIN CommentTag ct ON ct.comment = c
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:tags IS NULL OR ct.tag IN :tags)
        GROUP BY r
        ORDER BY MAX(v.createdAt) DESC NULLS LAST, r.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT r) FROM Place r
        LEFT JOIN Comment c ON c.place = r AND c.isDeleted = false
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
    ): Page<Place>

    // 이번 주 방문 많은 장소 TOP N
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE v.createdAt >= :weekStart AND v.createdAt < :weekEnd
        GROUP BY r
        ORDER BY COUNT(v) DESC
    """)
    fun findWeeklyTopPlaces(weekStart: LocalDateTime, weekEnd: LocalDateTime, pageable: Pageable): List<Place>

    // 누적 방문 많은 장소 TOP N
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        GROUP BY r
        ORDER BY COUNT(v) DESC
    """)
    fun findPopularPlaces(pageable: Pageable): List<Place>

    // 필터 + 거리순 정렬 (list 탭용, native query)
    @Query(
        value = """
            SELECT r.* FROM places r
            WHERE (CAST(:placeCategoryId AS BIGINT) IS NULL OR r.place_category_id = CAST(:placeCategoryId AS BIGINT))
            AND (CAST(:search AS TEXT) IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
            AND (CAST(:priceRange AS TEXT) IS NULL OR r.price_range = CAST(:priceRange AS TEXT))
            ORDER BY SQRT(POWER((r.lat - :userLat) * 111000, 2) + POWER((r.lng - :userLng) * 111000 * COS(RADIANS(:userLat)), 2)) ASC
        """,
        countQuery = """
            SELECT COUNT(*) FROM places r
            WHERE (CAST(:placeCategoryId AS BIGINT) IS NULL OR r.place_category_id = CAST(:placeCategoryId AS BIGINT))
            AND (CAST(:search AS TEXT) IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
            AND (CAST(:priceRange AS TEXT) IS NULL OR r.price_range = CAST(:priceRange AS TEXT))
        """,
        nativeQuery = true
    )
    fun findWithFiltersSortByDistance(
        placeCategoryId: Long?,
        search: String?,
        priceRange: String?,
        userLat: Double,
        userLng: Double,
        pageable: Pageable
    ): Page<Place>

    // 카테고리별 장소 수
    @Query("""
        SELECT r.placeCategoryId, COUNT(r)
        FROM Place r
        WHERE r.placeCategoryId IS NOT NULL
        GROUP BY r.placeCategoryId
    """)
    fun countByPlaceCategory(): List<Array<Any>>
}
