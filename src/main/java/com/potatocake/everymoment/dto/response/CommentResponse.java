package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private Long memberId;
    private CommentFriendResponse commentFriendResponse;
    private String content;
    private LocalDateTime createdAt;
}
