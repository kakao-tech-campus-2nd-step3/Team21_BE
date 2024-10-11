package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comments", description = "댓글 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    @PatchMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> updateComment(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "수정할 댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "댓글 수정 정보", required = true)
            @RequestBody CommentRequest commentRequest) {
        Long memberId = memberDetails.getId();

        commentService.updateComment(memberId, commentId, commentRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "삭제할 댓글 ID", required = true)
            @PathVariable Long commentId) {
        Long memberId = memberDetails.getId();

        commentService.deleteComment(memberId, commentId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
