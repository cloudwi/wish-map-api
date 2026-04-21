package com.mindbridge.wishmap.context.lunchvote.api

import com.mindbridge.wishmap.context.lunchvote.api.dto.*

import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import com.mindbridge.wishmap.context.lunchvote.application.LunchVoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups/{groupId}/lunch-vote")
class LunchVoteController(
    private val lunchVoteService: LunchVoteService
) {

    @GetMapping
    fun getActiveVote(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<LunchVoteResponse> {
        val vote = lunchVoteService.getActiveVote(principal.id, groupId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(vote)
    }

    @PostMapping
    fun createVote(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateLunchVoteRequest
    ): ResponseEntity<LunchVoteResponse> {
        val vote = lunchVoteService.createVote(principal.id, groupId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(vote)
    }

    @PostMapping("/candidates")
    fun addCandidate(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: AddCandidateRequest
    ): ResponseEntity<LunchVoteResponse> {
        return ResponseEntity.ok(lunchVoteService.addCandidate(principal.id, groupId, request))
    }

    @PostMapping("/vote")
    fun castVote(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CastVoteRequest
    ): ResponseEntity<LunchVoteResponse> {
        return ResponseEntity.ok(lunchVoteService.castVote(principal.id, groupId, request))
    }

    @DeleteMapping("/vote")
    fun retractVote(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Void> {
        lunchVoteService.retractVote(principal.id, groupId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/close")
    fun closeVote(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<LunchVoteResponse> {
        return ResponseEntity.ok(lunchVoteService.closeVote(principal.id, groupId))
    }
}
