package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members/{memberId}/friend-requests")
@RestController
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping
    public ResponseEntity<SuccessResponse> sendFriendRequest(@PathVariable Long memberId,
                                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails.getId().equals(memberId)) {
            throw new GlobalException(ErrorCode.SELF_FRIEND_REQUEST);
        }

        friendRequestService.sendFriendRequest(memberDetails.getId(), memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
