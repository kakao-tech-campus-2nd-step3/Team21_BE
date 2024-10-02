package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/likes")
@RestController
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<SuccessResponse> toggleLike(@PathVariable Long diaryId,
                                                      @AuthenticationPrincipal MemberDetails memberDetails) {
        likeService.toggleLike(memberDetails.getId(), diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
