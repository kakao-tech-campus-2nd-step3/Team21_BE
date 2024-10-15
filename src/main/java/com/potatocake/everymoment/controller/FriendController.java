package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    //특정 친구 일기 전체 조회
    @GetMapping("/{friendId}/diaries")
    public ResponseEntity<SuccessResponse<OneFriendDiariesResponse>> getOneFriendDiaries(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long friendId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = memberDetails.getId();

        OneFriendDiariesResponse response = friendService.OneFriendDiariesResponse(memberId, friendId, date, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //내 친구 목록 조회
    @GetMapping("/friends")
    public ResponseEntity<SuccessResponse<FriendListResponse>> getFriendList(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(required = false) String nickname,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = memberDetails.getId();

        FriendListResponse response = friendService.getFriendList(memberId, nickname, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //내 친구 삭제
    @DeleteMapping("/{friendId}")
    public ResponseEntity<SuccessResponse<Void>> deleteFriend(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long friendId) {
        Long memberId = memberDetails.getId();

        friendService.deleteFriend(memberId, friendId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //친한 친구 설정
    @PatchMapping("/{friendId}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleCloseFriend(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long friendId) {
        Long memberId = memberDetails.getId();

        friendService.toggleCloseFriend(memberId, friendId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
