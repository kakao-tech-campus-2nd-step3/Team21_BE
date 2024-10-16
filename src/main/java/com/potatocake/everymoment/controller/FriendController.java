package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Friends", description = "친구 관리 API")
@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @Operation(summary = "특정 친구 일기 전체 조회", description = "특정 친구의 모든 일기를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 일기 조회 성공", content = @Content(schema = @Schema(implementation = OneFriendDiariesResponse.class)))
    @GetMapping("/{friendId}/diaries")
    public ResponseEntity<SuccessResponse<OneFriendDiariesResponse>> getOneFriendDiaries(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "조회할 친구 ID", required = true)
            @PathVariable Long friendId,
            @Parameter(description = "조회할 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "페이지 키")
            @RequestParam(defaultValue = "0") int key,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = memberDetails.getId();

        OneFriendDiariesResponse response = friendService.OneFriendDiariesResponse(memberId, friendId, date, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "내 친구 목록 조회", description = "사용자의 친구 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 목록 조회 성공", content = @Content(schema = @Schema(implementation = FriendListResponse.class)))
    @GetMapping("/friends")
    public ResponseEntity<SuccessResponse<FriendListResponse>> getFriendList(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "검색할 친구 닉네임")
            @RequestParam(required = false) String nickname,
            @Parameter(description = "페이지 키")
            @RequestParam(defaultValue = "0") int key,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = memberDetails.getId();

        FriendListResponse response = friendService.getFriendList(memberId, nickname, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "친구 삭제", description = "특정 친구를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "친구 삭제 성공")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<SuccessResponse<Void>> deleteFriend(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "삭제할 친구 ID", required = true)
            @PathVariable Long friendId
    ) {
        Long memberId = memberDetails.getId();

        friendService.deleteFriend(memberId, friendId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "친한 친구 설정", description = "특정 친구를 친한 친구로 설정하거나 해제합니다.")
    @ApiResponse(responseCode = "200", description = "친한 친구 설정 성공")
    @PatchMapping("/{friendId}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleCloseFriend(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "설정할 친구 ID", required = true)
            @PathVariable Long friendId
    ) {
        Long memberId = memberDetails.getId();

        friendService.toggleCloseFriend(memberId, friendId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
