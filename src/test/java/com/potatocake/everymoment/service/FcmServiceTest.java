package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.potatocake.everymoment.dto.request.FcmNotificationRequest;
import com.potatocake.everymoment.entity.DeviceToken;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.repository.DeviceTokenRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @InjectMocks
    private FcmService fcmService;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("알림이 성공적으로 전송된다.")
    void should_SendNotification_When_ValidInput() throws Exception {
        // given
        Long targetMemberId = 1L;
        DeviceToken deviceToken = DeviceToken.builder()
                .fcmToken("fcm-token-123")
                .build();

        FcmNotificationRequest request = FcmNotificationRequest.builder()
                .title("Test Title")
                .body("Test Body")
                .type("TEST")
                .targetId(1L)
                .build();

        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse sendResponse = mock(SendResponse.class);
        given(sendResponse.isSuccessful()).willReturn(true);
        given(batchResponse.getResponses()).willReturn(List.of(sendResponse));

        given(deviceTokenRepository.findAllByMemberId(targetMemberId))
                .willReturn(List.of(deviceToken));
        given(firebaseMessaging.sendEach(any())).willReturn(batchResponse);

        // when
        fcmService.sendNotification(targetMemberId, request);

        // then
        then(firebaseMessaging).should().sendEach(any());
    }

    @Test
    @DisplayName("토큰이 성공적으로 등록된다.")
    void should_RegisterToken_When_ValidInput() {
        // given
        Long memberId = 1L;
        String deviceId = "device123";
        String fcmToken = "fcm-token-123";

        Member member = Member.builder()
                .id(memberId)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(deviceTokenRepository.findByMemberIdAndDeviceId(memberId, deviceId))
                .willReturn(Optional.empty());

        // when
        fcmService.registerToken(memberId, deviceId, fcmToken);

        // then
        then(deviceTokenRepository).should().save(any(DeviceToken.class));
    }

    @Test
    @DisplayName("기존 토큰이 성공적으로 업데이트된다.")
    void should_UpdateToken_When_TokenExists() {
        // given
        Long memberId = 1L;
        String deviceId = "device123";
        String fcmToken = "fcm-token-123";

        Member member = Member.builder()
                .id(memberId)
                .build();

        DeviceToken existingToken = DeviceToken.builder()
                .member(member)
                .deviceId(deviceId)
                .fcmToken("old-token")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(deviceTokenRepository.findByMemberIdAndDeviceId(memberId, deviceId))
                .willReturn(Optional.of(existingToken));

        // when
        fcmService.registerToken(memberId, deviceId, fcmToken);

        // then
        assertThat(existingToken.getFcmToken()).isEqualTo(fcmToken);
    }

    @Test
    @DisplayName("토큰이 성공적으로 삭제된다.")
    void should_RemoveToken_When_ValidInput() {
        // given
        Long memberId = 1L;
        String deviceId = "device123";

        willDoNothing().given(deviceTokenRepository)
                .deleteByMemberIdAndDeviceId(memberId, deviceId);

        // when
        fcmService.removeToken(memberId, deviceId);

        // then
        then(deviceTokenRepository).should().deleteByMemberIdAndDeviceId(memberId, deviceId);
    }

    @Test
    @DisplayName("잘못된 토큰은 자동으로 삭제된다.")
    void should_DeleteToken_When_TokenInvalid() throws Exception {
        // given
        Long targetMemberId = 1L;
        DeviceToken deviceToken = DeviceToken.builder()
                .fcmToken("invalid-token")
                .build();

        FcmNotificationRequest request = FcmNotificationRequest.builder()
                .title("Test Title")
                .body("Test Body")
                .type("TEST")
                .targetId(1L)
                .build();

        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse sendResponse = mock(SendResponse.class);
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);

        given(sendResponse.isSuccessful()).willReturn(false);
        given(sendResponse.getException()).willReturn(exception);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
        given(batchResponse.getResponses()).willReturn(List.of(sendResponse));

        given(deviceTokenRepository.findAllByMemberId(targetMemberId))
                .willReturn(List.of(deviceToken));
        given(firebaseMessaging.sendEach(any())).willReturn(batchResponse);

        // when
        fcmService.sendNotification(targetMemberId, request);

        // then
        then(deviceTokenRepository).should().deleteAll(any());
    }

}
