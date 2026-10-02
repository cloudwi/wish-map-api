package com.mindbridge.wishmap

import com.mindbridge.wishmap.context.identity.application.PhoneCodeService
import com.mindbridge.wishmap.context.identity.application.AuthService
import com.mindbridge.wishmap.context.identity.domain.PhoneVerificationRepository
import com.mindbridge.wishmap.context.identity.infrastructure.SmsSender
import com.mindbridge.wishmap.infrastructure.security.JwtTokenProvider
import com.mindbridge.wishmap.support.IntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

class PhoneCodeServiceIT : IntegrationTest() {
    @Autowired lateinit var codes: PhoneCodeService
    @Autowired lateinit var auth: AuthService
    @Autowired lateinit var verifications: PhoneVerificationRepository
    @Autowired lateinit var tokens: JwtTokenProvider
    @MockitoBean lateinit var sender: SmsSender

    @Test
    fun `code is single use and refresh token cannot authenticate API`() {
        val phone = "010" + UUID.randomUUID().toString().filter(Char::isDigit).padEnd(8, '1').take(8)
        codes.request(phone)
        val invocation = mockingDetails(sender).invocations.single { it.method.name == "sendCode" }
        assertEquals(phone, invocation.arguments[0])
        val code = invocation.arguments[1] as String
        assertFalse(codes.consume(phone, if (code == "000000") "000001" else "000000"))
        val user = auth.phoneLogin(phone, code).user
        assertFalse(codes.consume(phone, code))
        assertThrows(IllegalArgumentException::class.java) { codes.request(phone) }
        auth.deleteAccount(user.id)
        assertFalse(verifications.existsById(phone))
        val access = tokens.generateAccessToken(1)
        val refresh = tokens.generateRefreshToken(1)
        assertTrue(tokens.validateAccessToken(access))
        assertFalse(tokens.validateRefreshToken(access))
        assertTrue(tokens.validateRefreshToken(refresh))
        assertFalse(tokens.validateAccessToken(refresh))
    }
}
