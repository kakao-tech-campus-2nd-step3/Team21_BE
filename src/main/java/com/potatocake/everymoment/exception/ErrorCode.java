package com.potatocake.everymoment.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /* CategoryService */
    ALREADY_EXISTS_CATEGORY("이미 존재하는 카테고리입니다.", CONFLICT),

    /* Category */
    CATEGORY_NOT_FOUND("존재하지 않는 카테고리입니다.", NOT_FOUND),

    /* File */
    FILE_NOT_FOUND("존재하지 않는 파일입니다.", NOT_FOUND),
    FILE_SIZE_EXCEEDED("전체 파일 크기를 10MB 이하로 첨부해 주세요.", PAYLOAD_TOO_LARGE),

    /* Comment */
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다.", NOT_FOUND),

    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다.", INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
