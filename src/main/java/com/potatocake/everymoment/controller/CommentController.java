package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    //댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> updateComment(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest) {
        Long memberId = memberDetails.getId();

        commentService.updateComment(memberId, commentId, commentRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long commentId) {
        Long memberId = memberDetails.getId();

        commentService.deleteComment(memberId, commentId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
