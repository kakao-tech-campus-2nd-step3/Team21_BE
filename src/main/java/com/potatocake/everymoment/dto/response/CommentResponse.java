package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private CommentFriendResponse commentFriendResponse;
    private String content;
    private LocalDateTime createdAt;
}
