package com.potatocake.everymoment.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "dGVzdF9zZWNyZXRfdGVzdF9zZWNyZXRfdGVzdF9zZWNyZXRfdGVzdF9zZWNyZXRfdGVzdF9zZWNyZXQ=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        jwtUtil.init();
    }

    @Test
    @DisplayName("토큰이 성공적으로 생성된다.")
    void should_CreateToken_When_ValidInput() {
        // given
        Long id = 1L;

        // when
        String token = jwtUtil.create(id);

        // then
        assertThat(token).isNotEmpty();
        assertThatCode(() -> jwtUtil.getId(token))
                .doesNotThrowAnyException();
        assertThat(jwtUtil.getId(token)).isEqualTo(id);
    }

    @Test
    @DisplayName("유효한 토큰에서 ID가 성공적으로 추출된다.")
    void should_ExtractId_When_ValidToken() {
        // given
        Long expectedId = 1L;
        String token = jwtUtil.create(expectedId);

        // when
        Long extractedId = jwtUtil.getId(token);

        // then
        assertThat(extractedId).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("만료되지 않은 토큰은 유효하다고 판단된다.")
    void should_ReturnFalse_When_TokenNotExpired() {
        // given
        String token = jwtUtil.create(1L);

        // when
        boolean isExpired = jwtUtil.isExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰은 만료되었다고 판단된다.")
    void should_ReturnTrue_When_InvalidToken() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean isExpired = jwtUtil.isExpired(invalidToken);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Authorization 헤더에서 토큰이 성공적으로 추출된다.")
    void should_ResolveToken_When_ValidAuthorizationHeader() {
        // given
        String token = "valid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, jwtUtil.PREFIX + token);

        // when
        Optional<String> resolvedToken = jwtUtil.resolveToken(request);

        // then
        assertThat(resolvedToken)
                .isPresent()
                .contains(token);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 빈 Optional 이 반환된다.")
    void should_ReturnEmpty_When_NoAuthorizationHeader() {
        // given
        HttpServletRequest request = new MockHttpServletRequest();

        // when
        Optional<String> resolvedToken = jwtUtil.resolveToken(request);

        // then
        assertThat(resolvedToken).isEmpty();
    }

    @Test
    @DisplayName("Bearer 접두사가 없는 Authorization 헤더는 빈 Optional 을 반환한다.")
    void should_ReturnEmpty_When_NoBearerPrefix() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "invalid-format-token");

        // when
        Optional<String> resolvedToken = jwtUtil.resolveToken(request);

        // then
        assertThat(resolvedToken).isEmpty();
    }

}
