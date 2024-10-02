package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FriendRequestPageRequest;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @GetMapping("/api/friend-requests")
    public ResponseEntity<SuccessResponse<FriendRequestPageRequest>> getFriendRequests(
            @RequestParam(required = false) Long key,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        FriendRequestPageRequest friendRequests = friendRequestService.getFriendRequests(key, size,
                memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(friendRequests));
    }

    @PostMapping("/api/members/{memberId}/friend-requests")
    public ResponseEntity<SuccessResponse> sendFriendRequest(@PathVariable Long memberId,
                                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails.getId().equals(memberId)) {
            throw new GlobalException(ErrorCode.SELF_FRIEND_REQUEST);
        }

        friendRequestService.sendFriendRequest(memberDetails.getId(), memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @PostMapping("/api/friend-requests/{requestId}/accept")
    public ResponseEntity<SuccessResponse> acceptFriendRequest(@PathVariable Long requestId,
                                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        friendRequestService.acceptFriendRequest(requestId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @DeleteMapping("/api/friend-requests/{requestId}/reject")
    public ResponseEntity<SuccessResponse> rejectFriendRequest(@PathVariable Long requestId,
                                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        friendRequestService.rejectFriendRequest(requestId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
