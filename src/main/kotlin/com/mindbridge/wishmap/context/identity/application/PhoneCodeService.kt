package com.mindbridge.wishmap.context.identity.application

import com.mindbridge.wishmap.common.error.ServiceUnavailableException
import com.mindbridge.wishmap.context.identity.domain.PhoneVerification
import com.mindbridge.wishmap.context.identity.domain.PhoneVerificationRepository
import com.mindbridge.wishmap.context.identity.infrastructure.SmsSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class PhoneCodeService(
    private val verifications: PhoneVerificationRepository,
    private val sender: SmsSender,
    private val jdbc: JdbcTemplate,
    @Value("\${jwt.secret}") private val codeSecret: String,
    @Value("\${sms.max-daily-sends:20}") private val maxDailySends: Int
) {
    private val random = SecureRandom()

    fun normalize(input: String): String = input.replace("-", "").trim().also {
        require(it.matches(Regex("010[0-9]{8}"))) { "010으로 시작하는 휴대폰 번호를 입력해주세요." }
    }

    @Transactional
    fun request(input: String) {
        val phone = normalize(input)
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val entry = verifications.lockByPhone(phone) ?: verifications.saveAndFlush(PhoneVerification(phone, windowStartedAt = now))
        if (entry.windowStartedAt.isBefore(now.minusHours(1))) {
            entry.windowStartedAt = now
            entry.sentCount = 0
        }
        require(entry.lastSentAt == null || entry.lastSentAt!!.isBefore(now.minusSeconds(60))) { "1분 뒤에 다시 요청해주세요." }
        require(entry.sentCount < 5) { "인증번호 요청 횟수를 초과했습니다. 1시간 뒤 다시 시도해주세요." }
        val dailyCount = jdbc.query(
            """INSERT INTO sms_daily_quota(day, sent_count) VALUES(CURRENT_DATE, 1)
               ON CONFLICT(day) DO UPDATE SET sent_count = sms_daily_quota.sent_count + 1
               WHERE sms_daily_quota.sent_count < ? RETURNING sent_count""",
            { rs, _ -> rs.getInt(1) }, maxDailySends
        ).firstOrNull()
        if (dailyCount == null) throw ServiceUnavailableException("오늘의 인증 문자 발송 한도에 도달했습니다.")
        val code = "%06d".format(random.nextInt(1_000_000))
        sender.sendCode(phone, code)
        entry.codeHash = hash(phone, code)
        entry.expiresAt = now.plusMinutes(5)
        entry.attempts = 0
        entry.sentCount++
        entry.lastSentAt = now
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun consume(input: String, code: String): Boolean {
        val phone = normalize(input)
        require(code.matches(Regex("[0-9]{6}"))) { "6자리 인증번호를 입력해주세요." }
        val entry = verifications.lockByPhone(phone) ?: return false
        val expected = entry.codeHash ?: return false
        if (entry.expiresAt == null || !entry.expiresAt!!.isAfter(LocalDateTime.now(ZoneOffset.UTC)) || entry.attempts >= 5) return false
        val matches = MessageDigest.isEqual(expected.toByteArray(), hash(phone, code).toByteArray())
        if (matches) {
            entry.codeHash = null
            entry.expiresAt = null
        } else {
            entry.attempts++
        }
        return matches
    }

    private fun hash(phone: String, code: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(codeSecret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return HexFormat.of().formatHex(mac.doFinal("$phone:$code".toByteArray(Charsets.UTF_8)))
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    @Transactional
    fun purgeStale() {
        verifications.deleteStale(LocalDateTime.now(ZoneOffset.UTC).minusDays(1))
        jdbc.update("DELETE FROM sms_daily_quota WHERE day < CURRENT_DATE - INTERVAL '30 days'")
    }
}
