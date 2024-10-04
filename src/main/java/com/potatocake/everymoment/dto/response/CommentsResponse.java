package com.potatocake.everymoment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentsResponse {
    private List<CommentResponse> comments;
    private Integer next;
}

