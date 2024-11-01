package com.potatocake.everymoment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class FcmTokenRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Size(max = 512, message = "FCM 토큰은 512자를 넘을 수 없습니다.")
    private String fcmToken;

    @NotBlank(message = "디바이스 ID는 필수입니다.")
    @Size(max = 512, message = "디바이스 ID는 512자를 넘을 수 없습니다.")
    private String deviceId;

}
