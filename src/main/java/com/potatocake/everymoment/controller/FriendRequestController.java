package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FriendRequestPageRequest;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Friend Requests", description = "친구 요청 관리 API")
@RequiredArgsConstructor
@RestController
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @Operation(summary = "친구 요청 목록 조회", description = "사용자에게 온 친구 요청 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 목록 조회 성공", content = @Content(schema = @Schema(implementation = FriendRequestPageRequest.class)))
    @GetMapping("/api/friend-requests")
    public ResponseEntity<SuccessResponse<FriendRequestPageRequest>> getFriendRequests(
            @Parameter(description = "페이지 키")
            @RequestParam(required = false) Long key,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        FriendRequestPageRequest friendRequests = friendRequestService.getFriendRequests(key, size,
                memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(friendRequests));
    }

    @Operation(summary = "친구 요청 보내기", description = "특정 사용자에게 친구 요청을 보냅니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 전송 성공")
    @PostMapping("/api/members/{memberId}/friend-requests")
    public ResponseEntity<SuccessResponse> sendFriendRequest(
            @Parameter(description = "친구 요청을 받을 사용자 ID", required = true)
            @PathVariable Long memberId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        if (memberDetails.getId().equals(memberId)) {
            throw new GlobalException(ErrorCode.SELF_FRIEND_REQUEST);
        }

        friendRequestService.sendFriendRequest(memberDetails.getId(), memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공")
    @PostMapping("/api/friend-requests/{requestId}/accept")
    public ResponseEntity<SuccessResponse> acceptFriendRequest(
            @Parameter(description = "수락할 친구 요청 ID", required = true)
            @PathVariable Long requestId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        friendRequestService.acceptFriendRequest(requestId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공")
    @DeleteMapping("/api/friend-requests/{requestId}/reject")
    public ResponseEntity<SuccessResponse> rejectFriendRequest(
            @Parameter(description = "거절할 친구 요청 ID", required = true)
            @PathVariable Long requestId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        friendRequestService.rejectFriendRequest(requestId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
