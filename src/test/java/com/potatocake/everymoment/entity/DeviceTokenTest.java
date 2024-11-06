package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeviceTokenTest {

    @DisplayName("토큰값이 성공적으로 업데이트된다.")
    @Test
    void should_UpdateToken_When_NewTokenProvided() {
        // given
        DeviceToken deviceToken = DeviceToken.builder()
                .fcmToken("old-token")
                .deviceId("device123")
                .build();
        String newToken = "new-token";

        // when
        deviceToken.updateToken(newToken);

        // then
        assertThat(deviceToken.getFcmToken()).isEqualTo(newToken);
    }

}
