package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.NotificationListResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<NotificationListResponse>>> getNotifications(
            @AuthenticationPrincipal MemberDetails memberDetails){
        Long memberId = memberDetails.getId();

        List<NotificationListResponse> response = notificationService.getNotifications(memberId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<SuccessResponse<Void>> updateNotification(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long notificationId) {
        Long memberId = memberDetails.getId();

        notificationService.updateNotification(memberId, notificationId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
