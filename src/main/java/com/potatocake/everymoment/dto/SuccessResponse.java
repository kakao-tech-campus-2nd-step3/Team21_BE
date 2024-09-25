package com.potatocake.everymoment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class SuccessResponse<T> {

    private int code;
    private String message;
    private T info;

    public static <T> SuccessResponse of(int code, T info) {
        return SuccessResponse.builder()
                .code(code)
                .message("success")
                .info(info)
                .build();
    }

    public static <T> SuccessResponse ok(T info) {
        return SuccessResponse.builder()
                .code(200)
                .message("success")
                .info(info)
                .build();
    }

}
