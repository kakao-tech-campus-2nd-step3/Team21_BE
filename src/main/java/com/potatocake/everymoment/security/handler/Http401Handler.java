package com.potatocake.everymoment.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@RequiredArgsConstructor
public class Http401Handler implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ErrorCode loginRequired = ErrorCode.LOGIN_REQUIRED;

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(loginRequired.getStatus().value());

        mapper.writeValue(response.getWriter(), getErrorResponse(loginRequired));
    }

    private ErrorResponse getErrorResponse(ErrorCode loginRequired) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(loginRequired.getStatus().value())
                .message(loginRequired.getMessage())
                .build();

        return errorResponse;
    }

}
