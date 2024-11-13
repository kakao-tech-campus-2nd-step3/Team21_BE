package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FcmService fcmService;

    @Test
    @DisplayName("알림이 성공적으로 생성되고 전송된다.")
    void should_CreateAndSendNotification_When_ValidInput() {
        // given
        Long receiverId = 1L;
        Member receiver = Member.builder()
                .id(receiverId)
                .nickname("receiver")
                .build();

        given(memberRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            return Notification.builder()
                    .id(1L)
                    .member(notification.getMember())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .targetId(notification.getTargetId())
                    .build();
        });

        // when
        notificationService.createAndSendNotification(
                receiverId,
                NotificationType.COMMENT,
                1L,
                "testUser"
        );

        // then
        then(notificationRepository).should().save(any(Notification.class));
        then(fcmService).should().sendNotification(eq(receiverId), any(FcmNotificationRequest.class));
    }

    @Test
    @DisplayName("알림 목록이 성공적으로 조회된다.")
    void should_GetNotifications_When_ValidMemberId() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        List<Notification> notifications = List.of(
                Notification.builder()
                        .id(1L)
                        .member(member)
                        .content("Notification 1")
                        .type("TEST1")
                        .targetId(1L)
                        .isRead(false)
                        .build(),
                Notification.builder()
                        .id(2L)
                        .member(member)
                        .content("Notification 2")
                        .type("TEST2")
                        .targetId(2L)
                        .isRead(true)
                        .build()
        );

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(notificationRepository.findAllByMemberId(memberId, Sort.by(Sort.Direction.DESC, "createAt"))).willReturn(notifications);

        // when
        List<NotificationListResponse> responses = notificationService.getNotifications(memberId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("content")
                .containsExactly("Notification 1", "Notification 2");
        assertThat(responses).extracting("isRead")
                .containsExactly(false, true);
    }

    @Test
    @DisplayName("알림이 성공적으로 읽음 처리된다.")
    void should_UpdateNotification_When_ValidInput() {
        // given
        Long memberId = 1L;
        Long notificationId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Notification notification = Notification.builder()
                .id(notificationId)
                .member(member)
                .content("Test notification")
                .isRead(false)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        // when
        notificationService.updateNotification(memberId, notificationId);

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("다른 사용자의 알림을 읽음 처리하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_UpdateOtherUserNotification() {
        // given
        Long memberId = 1L;
        Long notificationId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member otherMember = Member.builder()
                .id(2L)
                .build();
        Notification notification = Notification.builder()
                .id(notificationId)
                .member(otherMember)  // 다른 사용자의 알림
                .content("Test notification")
                .isRead(false)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.updateNotification(memberId, notificationId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }
    
}
