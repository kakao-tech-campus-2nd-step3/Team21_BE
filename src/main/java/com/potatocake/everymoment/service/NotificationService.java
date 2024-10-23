package com.potatocake.everymoment.service;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.request.FcmNotificationRequest;
import com.potatocake.everymoment.dto.response.NotificationListResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.entity.Notification;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.repository.NotificationRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    public void createAndSendNotification(Long receiverId, NotificationType type, Long targetId,
                                          String... messageArgs) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        // DB에 알림 저장
        Notification notification = Notification.builder()
                .member(receiver)
                .content(type.formatMessage(messageArgs))
                .type(type.name())
                .targetId(targetId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // FCM 알림 발송
        try {
            fcmService.sendNotification(receiverId, FcmNotificationRequest.builder()
                    .title(type.getTitle())
                    .body(type.formatMessage(messageArgs))
                    .type(type.name())
                    .targetId(targetId)
                    .build());
        } catch (Exception e) {
            log.error("FCM 알림 발송 실패. receiverId: {}, type: {}", receiverId, type, e);
        }
    }

    public List<NotificationListResponse> getNotifications(Long memberId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findAllByMemberId(currentMember.getId());

        return notifications.stream()
                .map(this::convertToNotificationResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateNotification(Long memberId, Long notificationId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!Objects.equals(currentMember.getId(), notification.getMember().getId())) {
            throw new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.updateIsRead();
    }

    // 알림 DTO 변환
    private NotificationListResponse convertToNotificationResponseDTO(Notification notification) {
        return NotificationListResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .type(notification.getType())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreateAt())
                .build();
    }

}
