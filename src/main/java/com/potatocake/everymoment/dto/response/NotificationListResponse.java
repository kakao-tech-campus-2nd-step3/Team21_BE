package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationListResponse {
    private Long id;
    private String content;
    private boolean isRead;
    private String type;
    private Long targetId;
    private LocalDateTime createdAt;
}

