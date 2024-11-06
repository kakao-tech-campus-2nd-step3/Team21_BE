package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.DeviceToken;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class DeviceTokenRepositoryTest {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member createAndSaveMember() {
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/image.jpg")
                .build();
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("디바이스 토큰이 성공적으로 저장된다.")
    void should_SaveToken_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        DeviceToken token = DeviceToken.builder()
                .member(member)
                .deviceId("device123")
                .fcmToken("fcm-token-123")
                .build();

        // when
        DeviceToken savedToken = deviceTokenRepository.save(token);

        // then
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getDeviceId()).isEqualTo("device123");
        assertThat(savedToken.getFcmToken()).isEqualTo("fcm-token-123");
        assertThat(savedToken.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("회원의 모든 디바이스 토큰이 조회된다.")
    void should_FindAllTokens_When_FilteringByMemberId() {
        // given
        Member member = createAndSaveMember();
        DeviceToken token1 = DeviceToken.builder()
                .member(member)
                .deviceId("device1")
                .fcmToken("token1")
                .build();
        DeviceToken token2 = DeviceToken.builder()
                .member(member)
                .deviceId("device2")
                .fcmToken("token2")
                .build();

        deviceTokenRepository.saveAll(List.of(token1, token2));

        // when
        List<DeviceToken> tokens = deviceTokenRepository.findAllByMemberId(member.getId());

        // then
        assertThat(tokens).hasSize(2);
        assertThat(tokens).extracting("deviceId")
                .containsExactlyInAnyOrder("device1", "device2");
    }

    @Test
    @DisplayName("특정 디바이스의 토큰이 조회된다.")
    void should_FindToken_When_FilteringByMemberAndDevice() {
        // given
        Member member = createAndSaveMember();
        DeviceToken token = DeviceToken.builder()
                .member(member)
                .deviceId("device123")
                .fcmToken("fcm-token-123")
                .build();

        deviceTokenRepository.save(token);

        // when
        Optional<DeviceToken> foundToken = deviceTokenRepository
                .findByMemberIdAndDeviceId(member.getId(), "device123");

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getDeviceId()).isEqualTo("device123");
        assertThat(foundToken.get().getFcmToken()).isEqualTo("fcm-token-123");
    }

    @Test
    @DisplayName("존재하지 않는 디바이스의 토큰 조회시 빈 결과가 반환된다.")
    void should_ReturnEmpty_When_DeviceNotFound() {
        // given
        Member member = createAndSaveMember();

        // when
        Optional<DeviceToken> foundToken = deviceTokenRepository
                .findByMemberIdAndDeviceId(member.getId(), "non-existent-device");

        // then
        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("디바이스 토큰이 성공적으로 삭제된다.")
    void should_DeleteToken_When_ValidMemberAndDevice() {
        // given
        Member member = createAndSaveMember();
        DeviceToken token = DeviceToken.builder()
                .member(member)
                .deviceId("device123")
                .fcmToken("fcm-token-123")
                .build();

        deviceTokenRepository.save(token);

        // when
        deviceTokenRepository.deleteByMemberIdAndDeviceId(member.getId(), "device123");

        // then
        Optional<DeviceToken> deletedToken = deviceTokenRepository
                .findByMemberIdAndDeviceId(member.getId(), "device123");
        assertThat(deletedToken).isEmpty();
    }

    @Test
    @DisplayName("여러 디바이스 토큰이 한번에 삭제된다.")
    void should_DeleteAllTokens_When_DeletingMultipleTokens() {
        // given
        Member member = createAndSaveMember();
        List<DeviceToken> tokens = List.of(
                DeviceToken.builder()
                        .member(member)
                        .deviceId("device1")
                        .fcmToken("token1")
                        .build(),
                DeviceToken.builder()
                        .member(member)
                        .deviceId("device2")
                        .fcmToken("token2")
                        .build()
        );

        deviceTokenRepository.saveAll(tokens);

        // when
        deviceTokenRepository.deleteAll(tokens);

        // then
        List<DeviceToken> remainingTokens = deviceTokenRepository.findAllByMemberId(member.getId());
        assertThat(remainingTokens).isEmpty();
    }

    @Test
    @DisplayName("사용자별로 디바이스 토큰이 독립적으로 관리된다.")
    void should_ManageTokensSeparately_WhenMultipleUsers() {
        // given
        Member member1 = createAndSaveMember();
        Member member2 = Member.builder()
                .number(5678L)
                .nickname("testUser2")
                .profileImageUrl("https://example.com/image2.jpg")
                .build();
        memberRepository.save(member2);

        DeviceToken token1 = DeviceToken.builder()
                .member(member1)
                .deviceId("device1")
                .fcmToken("token1")
                .build();
        DeviceToken token2 = DeviceToken.builder()
                .member(member2)
                .deviceId("device1")
                .fcmToken("token2")
                .build();

        deviceTokenRepository.saveAll(List.of(token1, token2));

        // when
        List<DeviceToken> tokens1 = deviceTokenRepository.findAllByMemberId(member1.getId());
        List<DeviceToken> tokens2 = deviceTokenRepository.findAllByMemberId(member2.getId());

        // then
        assertThat(tokens1)
                .hasSize(1)
                .extracting("fcmToken")
                .containsExactly("token1");

        assertThat(tokens2)
                .hasSize(1)
                .extracting("fcmToken")
                .containsExactly("token2");
    }

}
