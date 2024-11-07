package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.entity.Notification;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("알림이 성공적으로 저장된다.")
    void should_SaveNotification_When_ValidEntity() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        Member savedMember = memberRepository.save(member);

        Notification notification = Notification.builder()
                .member(savedMember)
                .content("Test notification")
                .type("TEST")
                .targetId(1L)
                .isRead(false)
                .build();

        // when
        Notification savedNotification = notificationRepository.save(notification);

        // then
        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getContent()).isEqualTo("Test notification");
        assertThat(savedNotification.getMember()).isEqualTo(savedMember);
    }

    @Test
    @DisplayName("회원의 알림 목록이 성공적으로 조회된다.")
    void should_FindNotifications_When_FilteringByMemberId() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        Member savedMember = memberRepository.save(member);

        List<Notification> notifications = List.of(
                Notification.builder()
                        .member(savedMember)
                        .content("Notification 1")
                        .type("TEST1")
                        .targetId(1L)
                        .build(),
                Notification.builder()
                        .member(savedMember)
                        .content("Notification 2")
                        .type("TEST2")
                        .targetId(2L)
                        .build()
        );

        notificationRepository.saveAll(notifications);

        // when
        List<Notification> foundNotifications = notificationRepository
                .findAllByMemberId(savedMember.getId());

        // then
        assertThat(foundNotifications).hasSize(2);
        assertThat(foundNotifications)
                .extracting("content")
                .containsExactlyInAnyOrder("Notification 1", "Notification 2");
    }

    @Test
    @DisplayName("알림이 성공적으로 삭제된다.")
    void should_DeleteNotification_When_ValidEntity() {
        // given
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        Member savedMember = memberRepository.save(member);

        Notification notification = Notification.builder()
                .member(savedMember)
                .content("Test notification")
                .type("TEST")
                .targetId(1L)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // when
        notificationRepository.delete(savedNotification);

        // then
        List<Notification> remainingNotifications = notificationRepository
                .findAllByMemberId(savedMember.getId());
        assertThat(remainingNotifications).isEmpty();
    }

}
