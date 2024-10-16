package com.potatocake.everymoment.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    private final int code;
    private final String message;
    private final Map<String, String> validation;

    @Builder
    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.validation = new HashMap<>();
    }

    public void addValidation(String fieldName, String errorMessage) {
        this.validation.put(fieldName, errorMessage);
    }

}
