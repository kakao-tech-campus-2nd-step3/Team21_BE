package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.LikeCountResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Likes", description = "좋아요 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/likes")
@RestController
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 수 조회", description = "특정 일기의 좋아요 수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 수 조회 성공", content = @Content(schema = @Schema(implementation = LikeCountResponse.class)))
    @GetMapping
    public ResponseEntity<SuccessResponse<LikeCountResponse>> getLikeCount(
            @Parameter(description = "조회할 일기 ID", required = true)
            @PathVariable Long diaryId
    ) {
        LikeCountResponse likeCount = likeService.getLikeCount(diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(likeCount));
    }

    @Operation(summary = "좋아요 토글", description = "특정 일기에 대한 좋아요를 추가하거나 취소합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 토글 성공")
    @PostMapping
    public ResponseEntity<SuccessResponse> toggleLike(
            @Parameter(description = "토글할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        likeService.toggleLike(memberDetails.getId(), diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
