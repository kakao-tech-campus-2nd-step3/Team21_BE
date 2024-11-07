package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    @DisplayName("알림이 성공적으로 생성된다.")
    void should_CreateNotification_When_ValidInput() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();

        // when
        Notification notification = Notification.builder()
                .member(member)
                .content("Test notification")
                .type("TEST")
                .targetId(1L)
                .isRead(false)
                .build();

        // then
        assertThat(notification.getMember()).isEqualTo(member);
        assertThat(notification.getContent()).isEqualTo("Test notification");
        assertThat(notification.getType()).isEqualTo("TEST");
        assertThat(notification.getTargetId()).isEqualTo(1L);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("알림이 성공적으로 읽음 처리된다.")
    void should_MarkAsRead_When_UpdateIsRead() {
        // given
        Notification notification = Notification.builder()
                .content("Test notification")
                .isRead(false)
                .build();

        // when
        notification.updateIsRead();

        // then
        assertThat(notification.isRead()).isTrue();
    }

}
