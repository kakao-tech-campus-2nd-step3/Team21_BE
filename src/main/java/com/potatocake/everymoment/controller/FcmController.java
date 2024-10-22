package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.FcmTokenRequest;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FCM", description = "FCM 토큰 관리 API")
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "FCM 토큰 등록/갱신", description = "디바이스의 FCM 토큰을 등록하거나 갱신합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 등록/갱신 성공")
    @PostMapping("/token")
    public ResponseEntity<SuccessResponse> registerToken(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "FCM 토큰 정보", required = true)
            @RequestBody @Valid FcmTokenRequest request) {

        fcmService.registerToken(memberDetails.getId(), request.getDeviceId(), request.getFcmToken());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "FCM 토큰 삭제", description = "디바이스의 FCM 토큰을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 삭제 성공")
    @DeleteMapping("/token")
    public ResponseEntity<SuccessResponse> removeToken(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "디바이스 ID", required = true)
            @RequestParam String deviceId) {

        fcmService.removeToken(memberDetails.getId(), deviceId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
