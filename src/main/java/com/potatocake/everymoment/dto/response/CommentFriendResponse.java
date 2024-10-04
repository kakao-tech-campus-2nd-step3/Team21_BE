package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentFriendResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
}