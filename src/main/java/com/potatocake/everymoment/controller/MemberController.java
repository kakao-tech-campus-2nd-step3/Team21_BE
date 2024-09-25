package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/{memberId}")
    public ResponseEntity<SuccessResponse<MemberResponse>> memberInfo(@PathVariable Long memberId) {
        MemberResponse response = memberService.getMemberInfo(memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<Void>> updateMemberInfo(@AuthenticationPrincipal MemberDetails memberDetails,
                                                                  @RequestParam(required = false) MultipartFile profileImage,
                                                                  @RequestParam(required = false) String nickname) {
        validateProfileUpdate(profileImage, nickname);

        memberService.updateMemberInfo(memberDetails.getId(), profileImage, nickname);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    private void validateProfileUpdate(MultipartFile profileImage, String nickname) {
        if (profileImage == null && !StringUtils.hasText(nickname)) {
            throw new GlobalException(ErrorCode.INFO_REQUIRED);
        }
    }

}
