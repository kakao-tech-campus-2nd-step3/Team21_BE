package com.potatocake.everymoment.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.potatocake.everymoment.dto.request.FcmNotificationRequest;
import com.potatocake.everymoment.entity.DeviceToken;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DeviceTokenRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;

    public void sendNotification(Long targetMemberId, FcmNotificationRequest request) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByMemberId(targetMemberId);

        if (deviceTokens.isEmpty()) {
            log.warn("FCM 토큰이 존재하지 않는 사용자입니다. memberId: {}", targetMemberId);
            return;
        }

        List<Message> messages = deviceTokens.stream()
                .map(token -> Message.builder()
                        .setToken(token.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getBody())
                                .build())
                        .putData("type", request.getType())
                        .putData("targetId", request.getTargetId().toString())
                        .build())
                .collect(Collectors.toList());

        try {
            BatchResponse response = firebaseMessaging.sendEach(messages);
            handleBatchResponse(response, deviceTokens);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패. targetMemberId: {}, error: {}", targetMemberId, e.getMessage());
        }
    }

    private void handleBatchResponse(BatchResponse response, List<DeviceToken> deviceTokens) {
        List<DeviceToken> tokensToDelete = new ArrayList<>();

        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            DeviceToken deviceToken = deviceTokens.get(i);

            if (!sendResponse.isSuccessful()) {
                FirebaseMessagingException exception = sendResponse.getException();
                MessagingErrorCode errorCode = exception.getMessagingErrorCode();

                log.warn("FCM 토큰 전송 실패: {}. 에러 코드: {}, 메시지: {}",
                        deviceToken.getFcmToken(),
                        errorCode,
                        exception.getMessage());

                if (shouldDeleteToken(errorCode)) {
                    tokensToDelete.add(deviceToken);
                }
            }
        }

        if (!tokensToDelete.isEmpty()) {
            deviceTokenRepository.deleteAll(tokensToDelete);
            log.info("유효하지 않은 FCM 토큰 {} 개를 삭제했습니다", tokensToDelete.size());
        }
    }

    private boolean shouldDeleteToken(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED ||
                errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                errorCode == MessagingErrorCode.SENDER_ID_MISMATCH ||
                errorCode == MessagingErrorCode.THIRD_PARTY_AUTH_ERROR;
    }

    public void registerToken(Long memberId, String deviceId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        deviceTokenRepository.findByMemberIdAndDeviceId(memberId, deviceId)
                .ifPresentOrElse(
                        deviceToken -> deviceToken.updateToken(fcmToken),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .member(member)
                                .deviceId(deviceId)
                                .fcmToken(fcmToken)
                                .build())

                );
    }

    public void removeToken(Long memberId, String deviceId) {
        deviceTokenRepository.deleteByMemberIdAndDeviceId(memberId, deviceId);
    }

}
