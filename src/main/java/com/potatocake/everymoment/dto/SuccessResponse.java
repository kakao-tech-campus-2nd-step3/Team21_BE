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

    public static <T> SuccessResponse of(T info) {
        return SuccessResponse.builder()
                .message("success")
                .info(info)
                .build();
    }

}
