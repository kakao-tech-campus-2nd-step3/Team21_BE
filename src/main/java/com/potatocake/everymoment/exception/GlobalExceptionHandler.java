package com.potatocake.everymoment.exception;

import static com.potatocake.everymoment.exception.ErrorCode.FILE_SIZE_EXCEEDED;
import static com.potatocake.everymoment.exception.ValidationErrorMessage.VALIDATION_ERROR;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = getErrorResponse(400, VALIDATION_ERROR);

        e.getFieldErrors().forEach(filedError ->
                errorResponse.addValidation(filedError.getField(), filedError.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> missingRequestPart(MissingServletRequestPartException e) {
        ErrorResponse errorResponse = getErrorResponse(400, e.getMessage());

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> fileMaxSize() {
        ErrorResponse errorResponse = getErrorResponse(PAYLOAD_TOO_LARGE.value(), FILE_SIZE_EXCEEDED.getMessage());

        return ResponseEntity.status(PAYLOAD_TOO_LARGE)
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
