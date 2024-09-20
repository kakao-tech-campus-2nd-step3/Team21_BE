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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;

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
        MemberLoginRequest loginRequest = getLoginRequest(request);

        if (loginRequest.getEmail() != null && loginRequest.getNickname() != null) {
            registerIfNotExists(loginRequest);
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.getEmail(), DEFAULT_PASSWORD);

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

        objectMapper.writeValue(response.getWriter(), SuccessResponse.of(jwt));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        ErrorCode loginFailed = ErrorCode.LOGIN_FAILED;

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(loginFailed.getStatus().value());

        objectMapper.writeValue(response.getWriter(), getErrorResponse(loginFailed));
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
        if (!memberRepository.existsByEmail(loginRequest.getEmail())) {
            memberRepository.save(getMember(loginRequest));
        }
    }

    private Member getMember(MemberLoginRequest loginRequest) {
        Member member = Member.builder()
                .email(loginRequest.getEmail())
                .nickname(loginRequest.getNickname())
                .build();

        return member;
    }

}
