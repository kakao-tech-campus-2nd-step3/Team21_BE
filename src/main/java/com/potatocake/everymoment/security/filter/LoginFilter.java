package com.potatocake.everymoment.security.filter;

import static com.potatocake.everymoment.constant.Constants.DEFAULT_PASSWORD;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.MemberLoginRequest;
import com.potatocake.everymoment.dto.response.JwtResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.ErrorResponse;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;

    private final boolean postOnly = true;

    public LoginFilter(String filterProcessesUrl, ObjectMapper objectMapper, JwtUtil jwtUtil,
                       MemberRepository memberRepository, AuthenticationManager authenticationManager) {
        setFilterProcessesUrl(filterProcessesUrl);
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        MemberLoginRequest loginRequest = getLoginRequest(request);

        if (loginRequest.getNumber() == null || loginRequest.getNickname() == null) {
            throw new AuthenticationServiceException("Invalid login request");
        } else {
            registerIfNotExists(loginRequest);
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                String.valueOf(loginRequest.getNumber()), DEFAULT_PASSWORD);

        return authenticationManager.authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        MemberDetails memberDetails = (MemberDetails) authResult.getPrincipal();
        Member member = memberDetails.getMember();

        String token = jwtUtil.create(member.getId());

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(SC_OK);

        JwtResponse jwt = JwtResponse.of(token);

        objectMapper.writeValue(response.getWriter(), SuccessResponse.ok(jwt));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        ErrorCode errorCode;

        if (failed instanceof AuthenticationServiceException &&
                failed.getMessage().startsWith("Authentication method not supported")) {
            errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        } else {
            errorCode = ErrorCode.LOGIN_FAILED;
        }

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());

        objectMapper.writeValue(response.getWriter(), getErrorResponse(errorCode));
    }

    private MemberLoginRequest getLoginRequest(HttpServletRequest request) {
        try {
            return objectMapper.readValue(request.getInputStream(), MemberLoginRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ErrorResponse getErrorResponse(ErrorCode loginFailed) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(loginFailed.getStatus().value())
                .message(loginFailed.getMessage())
                .build();

        return errorResponse;
    }

    private void registerIfNotExists(MemberLoginRequest loginRequest) {
        if (!memberRepository.existsByNumber(loginRequest.getNumber())) {
            memberRepository.save(getMember(loginRequest));
        }
    }

    private Member getMember(MemberLoginRequest loginRequest) {
        Member member = Member.builder()
                .number(loginRequest.getNumber())
                .nickname(loginRequest.getNickname())
                .build();

        return member;
    }

}
