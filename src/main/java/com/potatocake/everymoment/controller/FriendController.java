package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.service.FriendService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    //특정 친구 일기 전체 조회
    @GetMapping("/{id}/diaries")
    public ResponseEntity<SuccessResponse<OneFriendDiariesResponse>> getOneFriendDiaries(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size) {
        OneFriendDiariesResponse diaries = friendService.OneFriendDiariesResponse(id, date, key, size);
        SuccessResponse<OneFriendDiariesResponse> response = SuccessResponse.<OneFriendDiariesResponse>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(diaries)
                .build();
        return ResponseEntity.ok(response);
    }

    //내 친구 목록 조회
    @GetMapping("/friends")
    public ResponseEntity<SuccessResponse<FriendListResponse>> getFriendList(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size) {
        FriendListResponse friendList = friendService.getFriendList(nickname, email, key, size);
        SuccessResponse<FriendListResponse> response = SuccessResponse.<FriendListResponse>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(friendList)
                .build();
        return ResponseEntity.ok(response);
    }

    //내 친구 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }

    //친한 친구 설정
    @PatchMapping("/{id}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleCloseFriend(@PathVariable Long id) {
        friendService.toggleCloseFriend(id);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
