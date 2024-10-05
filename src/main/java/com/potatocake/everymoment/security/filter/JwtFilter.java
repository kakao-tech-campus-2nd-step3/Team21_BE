package com.potatocake.everymoment.security.filter;

import static com.potatocake.everymoment.constant.Constants.DEFAULT_PASSWORD;

import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> token = jwtUtil.resolveToken(request);

        if (token.isEmpty() || jwtUtil.isExpired(token.get())) {
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUser(jwtUtil.getId(token.get()));

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(Long id) {
        MemberDetails memberDetails = new MemberDetails(Member.builder().id(id).build());

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                memberDetails, DEFAULT_PASSWORD, memberDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
