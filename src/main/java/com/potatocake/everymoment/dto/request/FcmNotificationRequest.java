package com.potatocake.everymoment.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmNotificationRequest {

    private String title;
    private String body;
    private String type;
    private Long targetId;

}
