package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

}
