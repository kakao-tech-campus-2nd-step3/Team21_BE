package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<SuccessResponse<MemberSearchResponse>> searchMembers(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long key,
            @RequestParam(defaultValue = "10") int size) {

        MemberSearchResponse response = memberService.searchMembers(nickname, email, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MemberDetailResponse>> myInfo(
            @AuthenticationPrincipal MemberDetails memberDetails) {
        MemberDetailResponse response = memberService.getMyInfo(memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

}
