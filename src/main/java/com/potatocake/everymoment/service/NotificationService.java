package com.potatocake.everymoment.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

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

        if(!Objects.equals(currentMember.getId(), notification.getMemberId().getId())){
            throw new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.updateIsRead();
    }

    // 알림 DTO 변환
    private NotificationListResponse convertToNotificationResponseDTO(Notification notification){
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
