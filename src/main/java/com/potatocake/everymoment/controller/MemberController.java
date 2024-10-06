package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.MemberLoginRequest;
import com.potatocake.everymoment.dto.response.JwtResponse;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "로그인", description = "회원 번호와 닉네임으로 로그인합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "로그인 성공",
//                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
//            @ApiResponse(responseCode = "401", description = "로그인 실패",
//                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    })
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<JwtResponse>> login(@RequestBody MemberLoginRequest request) {
        // 이 메서드는 실제로 호출되지 않습니다. Swagger 문서화를 위해서만 존재합니다.
        // 실제 로그인 처리는 LoginFilter에서 이루어집니다.
        return ResponseEntity.ok(SuccessResponse.ok(JwtResponse.of("token")));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<MemberSearchResponse>> searchMembers(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long key,
            @RequestParam(defaultValue = "10") int size) {

        MemberSearchResponse response = memberService.searchMembers(nickname, key, size);

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<Void>> updateMemberInfo(@AuthenticationPrincipal MemberDetails memberDetails,
                                                                  @RequestParam(required = false) MultipartFile profileImage,
                                                                  @RequestParam(required = false) String nickname) {
        validateProfileUpdate(profileImage, nickname);

        memberService.updateMemberInfo(memberDetails.getId(), profileImage, nickname);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.deleteMember(memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    private void validateProfileUpdate(MultipartFile profileImage, String nickname) {
        boolean isProfileImageEmpty = profileImage == null || profileImage.isEmpty();
        boolean isNicknameEmpty = !StringUtils.hasText(nickname);
        
        if (isProfileImageEmpty && isNicknameEmpty) {
            throw new GlobalException(ErrorCode.INFO_REQUIRED);
        }
    }

}
