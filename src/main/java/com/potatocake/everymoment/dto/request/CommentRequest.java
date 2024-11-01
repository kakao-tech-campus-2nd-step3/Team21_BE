package com.potatocake.everymoment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 250, message = "댓글은 250자를 초과할 수 없습니다")
    private String content;

}
