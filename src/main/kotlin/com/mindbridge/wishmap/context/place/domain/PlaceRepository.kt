package com.mindbridge.wishmap.context.place.domain

import com.mindbridge.wishmap.context.place.domain.PriceRange
import com.mindbridge.wishmap.context.place.domain.Place
import com.mindbridge.wishmap.context.identity.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

// 무한 스크롤 기반 목록이므로 Page 대신 Slice 사용.
// Slice는 totalElements/totalPages 계산용 COUNT 쿼리를 실행하지 않아
// 복잡한 LEFT JOIN + GROUP BY 쿼리에서 응답 속도가 크게 개선됨.
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
    ): Slice<Place>

    @Query("""
        SELECT DISTINCT r FROM Place r
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
    ): Slice<Place>

    fun findBySuggestedBy(user: User, pageable: Pageable): Slice<Place>

    // Keyset pagination: 내 제보 목록 (createdAt, id) DESC 커서.
    @Query("""
        SELECT r FROM Place r
        WHERE r.suggestedBy = :user
        AND (:cursorCreatedAt IS NULL
             OR r.createdAt < :cursorCreatedAt
             OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
        ORDER BY r.createdAt DESC, r.id DESC
    """)
    fun findBySuggestedByCursor(
        user: User,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        pageable: Pageable
    ): Slice<Place>

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
    ): Slice<Place>

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
    """)
    fun findWithFilters(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        pageable: Pageable
    ): Slice<Place>

    // Keyset pagination: 기본 최신순 정렬에만 적용 (sortBy=visits/recentVisit/distance는 복합 커서 필요, Step 3).
    // tags 필터는 EXISTS로 적용 (DISTINCT cartesian 회피).
    @Query("""
        SELECT r FROM Place r
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:hasTagFilter = false OR EXISTS (
            SELECT 1 FROM Comment c JOIN c.tags ct
            WHERE c.place = r AND c.isDeleted = false AND ct.tag IN :tags
        ))
        AND (:cursorCreatedAt IS NULL
             OR r.createdAt < :cursorCreatedAt
             OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
        ORDER BY r.createdAt DESC, r.id DESC
    """)
    fun findWithFiltersByCursor(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        hasTagFilter: Boolean,
        tags: List<String>,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        pageable: Pageable
    ): Slice<Place>

    // 필터 + 검색 + 방문 수 정렬 (tags 없음 - Comment/CommentTag 조인 없이 고속 실행)
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        GROUP BY r
        ORDER BY COUNT(v) DESC, r.createdAt DESC
    """)
    fun findWithFiltersSortByVisits(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        pageable: Pageable
    ): Slice<Place>

    // 필터 + 검색 + 태그 + 방문 수 정렬 (태그 포함)
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND EXISTS (
            SELECT 1 FROM Comment c JOIN c.tags ct
            WHERE c.place = r AND c.isDeleted = false AND ct.tag IN :tags
        )
        GROUP BY r
        ORDER BY COUNT(v) DESC, r.createdAt DESC
    """)
    fun findWithFiltersSortByVisitsWithTags(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>,
        pageable: Pageable
    ): Slice<Place>

    // Keyset pagination + sortBy=visits. 복합 커서: (visitCount, id).
    // HAVING 절로 GROUP BY 집계 이후 커서를 적용.
    // hasTagFilter=true일 때만 EXISTS(CommentTag) 적용, 태그 미사용 시 불필요한 JOIN 회피.
    @Query("""
        SELECT r FROM Place r
        LEFT JOIN Visit v ON v.place = r
        WHERE (:placeCategoryId IS NULL OR r.placeCategoryId = :placeCategoryId)
        AND (:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:priceRange IS NULL OR r.priceRange = :priceRange)
        AND (:hasTagFilter = false OR EXISTS (
            SELECT 1 FROM Comment c JOIN c.tags ct
            WHERE c.place = r AND c.isDeleted = false AND ct.tag IN :tags
        ))
        GROUP BY r
        HAVING :cursorVisitCount IS NULL
            OR COUNT(v) < :cursorVisitCount
            OR (COUNT(v) = :cursorVisitCount AND r.id < :cursorId)
        ORDER BY COUNT(v) DESC, r.id DESC
    """)
    fun findWithFiltersSortByVisitsCursor(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        hasTagFilter: Boolean,
        tags: List<String>,
        cursorVisitCount: Long?,
        cursorId: Long?,
        pageable: Pageable
    ): Slice<Place>

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
    """)
    fun findWithFiltersSortByRecentVisit(
        placeCategoryId: Long?,
        search: String?,
        priceRange: PriceRange?,
        tags: List<String>?,
        pageable: Pageable
    ): Slice<Place>

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
        nativeQuery = true
    )
    fun findWithFiltersSortByDistance(
        placeCategoryId: Long?,
        search: String?,
        priceRange: String?,
        userLat: Double,
        userLng: Double,
        pageable: Pageable
    ): Slice<Place>

    // 카테고리별 장소 수
    @Query("""
        SELECT r.placeCategoryId, COUNT(r)
        FROM Place r
        WHERE r.placeCategoryId IS NOT NULL
        GROUP BY r.placeCategoryId
    """)
    fun countByPlaceCategory(): List<Array<Any>>
}
