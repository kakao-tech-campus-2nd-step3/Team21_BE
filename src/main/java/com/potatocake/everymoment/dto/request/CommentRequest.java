package com.potatocake.everymoment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class CommentRequest {

    @NotEmpty(message = "댓글 내용을 입력해 주세요.")
    private String content;

}
