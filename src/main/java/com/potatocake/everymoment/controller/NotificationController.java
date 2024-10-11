package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.NotificationListResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "알림 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공", content = @Content(schema = @Schema(implementation = NotificationListResponse.class)))
    @GetMapping
    public ResponseEntity<SuccessResponse<List<NotificationListResponse>>> getNotifications(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<NotificationListResponse> response = notificationService.getNotifications(memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    @PatchMapping("/{notificationId}")
    public ResponseEntity<SuccessResponse<Void>> updateNotification(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "읽음 처리할 알림 ID", required = true)
            @PathVariable Long notificationId
    ) {
        Long memberId = memberDetails.getId();

        notificationService.updateNotification(memberId, notificationId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
    
}
