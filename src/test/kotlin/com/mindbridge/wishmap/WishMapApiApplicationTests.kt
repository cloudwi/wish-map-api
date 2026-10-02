package com.mindbridge.wishmap

import com.mindbridge.wishmap.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class WishMapApiApplicationTests : IntegrationTest() {
    @Autowired lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun contextLoads() {
        // Testcontainer 기동 + 전체 Flyway 마이그레이션 + 스프링 컨텍스트 로드 확인
    }

    @Test
    fun legacyTablesAreRemovedAndPhoneLoginTablesExist() {
        listOf(
            "lunch_votes", "lunch_vote_candidates", "lunch_vote_selections",
            "friends", "groups", "group_members", "visits", "comments",
            "comment_tags", "comment_images", "place_categories", "trend_tags", "places", "restaurant_images", "social_accounts"
        ).forEach { table ->
            assertNull(jdbcTemplate.queryForObject("SELECT to_regclass('public.$table')::text", String::class.java))
        }
        listOf("users", "phone_verifications", "parties", "party_members", "notifications", "reports", "blocked_users")
            .forEach { table ->
                assertNotNull(jdbcTemplate.queryForObject("SELECT to_regclass('public.$table')::text", String::class.java))
            }
    }
}
