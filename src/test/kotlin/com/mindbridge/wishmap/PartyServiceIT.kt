package com.mindbridge.wishmap

import com.mindbridge.wishmap.common.error.ForbiddenException
import com.mindbridge.wishmap.context.identity.domain.User
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import com.mindbridge.wishmap.context.party.application.CreatePartyRequest
import com.mindbridge.wishmap.context.party.application.PartyService
import com.mindbridge.wishmap.support.IntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class PartyServiceIT : IntegrationTest() {
    @Autowired lateinit var users: UserRepository
    @Autowired lateinit var service: PartyService

    @Test
    fun `guests browse while signed in users create and join with host approval`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val host = users.save(User("host_$suffix"))
        val guest = users.save(User("guest_$suffix"))
        val request = CreatePartyRequest("볼링 같이 쳐요", "초보도 환영", "BOWLING", "테스트 볼링장",
            "서울시 관악구 테스트로 1", 37.48, 126.95, OffsetDateTime.now(ZoneOffset.UTC).plusDays(2), 2)
        val party = service.create(host.id, request)
        assertTrue(service.list(10, null).any { it.id == party.id })
        assertTrue(service.list(10, "볼링장").any { it.id == party.id })
        assertFalse(service.list(10, "찾을수없는장소").any { it.id == party.id })
        assertEquals(1, service.detail(party.id, null).participantCount)
        assertThrows(ForbiddenException::class.java) { service.join(-1, party.id) }
        assertEquals("PENDING", service.join(guest.id, party.id).members.single().status)
        assertTrue(service.detail(party.id, null).members.isEmpty())
        val pending = service.detail(party.id, host.id).members.single()
        assertEquals("PENDING", pending.status)
        assertThrows(ForbiddenException::class.java) { service.decide(guest.id, party.id, pending.id, true) }
        service.decide(host.id, party.id, pending.id, true)
        assertEquals(2, service.detail(party.id, null).participantCount)
        assertEquals("APPROVED", service.detail(party.id, null).members.single().status)
    }
}
