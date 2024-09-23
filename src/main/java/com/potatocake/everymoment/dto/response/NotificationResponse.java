package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private String content;
    private String type;
    private Long targetId;
    private LocalDateTime createAt;
}