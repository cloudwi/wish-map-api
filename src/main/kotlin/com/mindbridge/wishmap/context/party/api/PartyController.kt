package com.mindbridge.wishmap.context.party.api

import com.mindbridge.wishmap.context.party.application.CreatePartyRequest
import com.mindbridge.wishmap.context.party.application.PartyResponse
import com.mindbridge.wishmap.context.party.application.PartyService
import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/parties")
class PartyController(private val service: PartyService) {
    @GetMapping
    fun list(@RequestParam(defaultValue = "50") limit: Int, @RequestParam(required = false) query: String?): List<PartyResponse> = service.list(limit, query)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, @AuthenticationPrincipal user: UserPrincipal?): PartyResponse = service.detail(id, user?.id)

    @PostMapping
    fun create(@AuthenticationPrincipal user: UserPrincipal, @RequestBody request: CreatePartyRequest): ResponseEntity<PartyResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(user.id, request))

    @PostMapping("/{id}/join")
    fun join(@AuthenticationPrincipal user: UserPrincipal, @PathVariable id: Long): PartyResponse = service.join(user.id, id)

    @PostMapping("/{id}/members/{memberId}/approve")
    fun approve(@AuthenticationPrincipal user: UserPrincipal, @PathVariable id: Long, @PathVariable memberId: Long): PartyResponse =
        service.decide(user.id, id, memberId, true)

    @PostMapping("/{id}/members/{memberId}/reject")
    fun reject(@AuthenticationPrincipal user: UserPrincipal, @PathVariable id: Long, @PathVariable memberId: Long): PartyResponse =
        service.decide(user.id, id, memberId, false)

    @PostMapping("/{id}/withdraw")
    fun withdraw(@AuthenticationPrincipal user: UserPrincipal, @PathVariable id: Long): PartyResponse = service.withdraw(user.id, id)

    @PostMapping("/{id}/cancel")
    fun cancel(@AuthenticationPrincipal user: UserPrincipal, @PathVariable id: Long): PartyResponse = service.cancel(user.id, id)
}
