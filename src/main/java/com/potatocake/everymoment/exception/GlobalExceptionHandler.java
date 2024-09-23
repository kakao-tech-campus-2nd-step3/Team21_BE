package com.potatocake.everymoment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(400)
                .message(ValidationErrorMessage.VALIDATION_ERROR)
                .build();

        e.getFieldErrors().forEach(filedError ->
                errorResponse.addValidation(filedError.getField(), filedError.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handlerCustomException(GlobalException e) {
        HttpStatus status = e.getErrorCode().getStatus();

        ErrorResponse errorResponse = getErrorResponse(status.value(), e.getMessage());

        return ResponseEntity.status(status)
                .body(errorResponse);
    }

    private ErrorResponse getErrorResponse(int code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

}
