package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.MemberLoginRequest;
import com.potatocake.everymoment.dto.response.JwtResponse;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberMyResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Members", description = "회원 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "로그인", description = "회원 번호와 닉네임으로 로그인합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = JwtResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<JwtResponse>> login(
            @Parameter(description = "로그인 정보", required = true)
            @RequestBody MemberLoginRequest request
    ) {
        // 이 메서드는 실제로 호출되지 않습니다. Swagger 문서화를 위해서만 존재합니다.
        // 실제 로그인 처리는 LoginFilter에서 이루어집니다.
        return ResponseEntity.ok(SuccessResponse.ok(JwtResponse.of("token")));
    }

    @Operation(summary = "회원 검색", description = "닉네임으로 회원을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "회원 검색 성공", content = @Content(schema = @Schema(implementation = MemberSearchResponse.class)))
    @GetMapping
    public ResponseEntity<SuccessResponse<MemberSearchResponse>> searchMembers(
            @Parameter(description = "검색할 닉네임")
            @RequestParam(required = false) String nickname,
            @Parameter(description = "페이지 키")
            @RequestParam(required = false) Long key,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        MemberSearchResponse response = memberService.searchMembers(nickname, key, size, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 회원의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공", content = @Content(schema = @Schema(implementation = MemberDetailResponse.class)))
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MemberDetailResponse>> myInfo(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        MemberDetailResponse response = memberService.getMyInfo(memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공", content = @Content(schema = @Schema(implementation = MemberMyResponse.class)))
    @GetMapping("/{memberId}")
    public ResponseEntity<SuccessResponse<MemberMyResponse>> memberInfo(
            @Parameter(description = "조회할 회원 ID", required = true)
            @PathVariable Long memberId
    ) {
        MemberMyResponse response = memberService.getMemberInfo(memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 회원의 프로필 이미지와 닉네임을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<Void>> updateMemberInfo(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "프로필 이미지 파일")
            @RequestParam(required = false) MultipartFile profileImage,
            @Parameter(description = "새로운 닉네임")
            @RequestParam(required = false) String nickname
    ) {
        validateProfileUpdate(profileImage, nickname);

        memberService.updateMemberInfo(memberDetails.getId(), profileImage, nickname);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 회원의 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteMember(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
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
