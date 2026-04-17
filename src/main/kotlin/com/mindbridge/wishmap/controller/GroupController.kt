package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.domain.place.PriceRange
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.GroupService
import com.mindbridge.wishmap.service.PlaceService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups")
class GroupController(
    private val groupService: GroupService,
    private val placeService: PlaceService
) {

    @GetMapping
    fun getMyGroups(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<List<GroupResponse>> =
        ResponseEntity.ok(groupService.getMyGroups(user.id))

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateGroupRequest
    ): ResponseEntity<GroupResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(user.id, request))

    @GetMapping("/{id}")
    fun getGroupDetail(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<GroupDetailResponse> =
        ResponseEntity.ok(groupService.getGroupDetail(user.id, id))

    @PostMapping("/{id}/members")
    fun inviteMember(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: InviteMemberRequest
    ): ResponseEntity<GroupMemberResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(groupService.inviteMember(user.id, id, request.nickname))

    @DeleteMapping("/{id}/members/{userId}")
    fun kickMember(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Void> {
        groupService.kickMember(user.id, id, userId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/transfer")
    fun transferLeader(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: TransferLeaderRequest
    ): ResponseEntity<Void> {
        groupService.transferLeader(user.id, id, request.newLeaderId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}/leave")
    fun leaveGroup(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        groupService.leaveGroup(user.id, id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/invites")
    fun getPendingInvites(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<List<GroupInviteResponse>> =
        ResponseEntity.ok(groupService.getPendingInvites(user.id))

    @PatchMapping("/{id}/invites/accept")
    fun acceptInvite(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        groupService.acceptInvite(user.id, id)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{id}/invites/reject")
    fun rejectInvite(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        groupService.rejectInvite(user.id, id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/location")
    fun updateGroupLocation(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateGroupLocationRequest
    ): ResponseEntity<Void> {
        groupService.updateGroupLocation(user.id, id, request)
        return ResponseEntity.ok().build()
    }

    // 그룹 필터: 그룹 구성원이 방문/제보한 맛집만 조회
    @GetMapping("/{id}/places")
    fun getGroupPlaces(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long,
        @RequestParam minLat: Double,
        @RequestParam maxLat: Double,
        @RequestParam minLng: Double,
        @RequestParam maxLng: Double,
        @RequestParam(required = false) priceRange: String?,
        @PageableDefault(size = 50) pageable: Pageable
    ): ResponseEntity<Page<PlaceListResponse>> {
        val parsedPriceRange = priceRange?.let {
            try { PriceRange.valueOf(it) } catch (_: IllegalArgumentException) { null }
        }
        val memberIds = groupService.getGroupMemberIds(id)
        return ResponseEntity.ok(
            placeService.getPlacesByMembers(minLat, maxLat, minLng, maxLng, memberIds, parsedPriceRange, pageable)
        )
    }
}
