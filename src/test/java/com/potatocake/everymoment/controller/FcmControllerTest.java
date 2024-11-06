package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.request.FcmTokenRequest;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FcmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FcmController.class)
class FcmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FcmService fcmService;

    @Test
    @DisplayName("FCM 토큰이 성공적으로 등록된다.")
    void should_RegisterToken_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FcmTokenRequest request = new FcmTokenRequest("fcm-token-123", "device123");

        willDoNothing().given(fcmService).registerToken(memberId, "device123", "fcm-token-123");

        // when
        ResultActions result = mockMvc.perform(post("/api/fcm/token")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(fcmService).should().registerToken(memberId, "device123", "fcm-token-123");
    }

    @Test
    @DisplayName("FCM 토큰이 성공적으로 삭제된다.")
    void should_RemoveToken_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);
        String deviceId = "device123";

        willDoNothing().given(fcmService).removeToken(memberId, deviceId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/fcm/token")
                .with(user(memberDetails))
                .with(csrf())
                .param("deviceId", deviceId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(fcmService).should().removeToken(memberId, deviceId);
    }

    @Test
    @DisplayName("FCM 토큰 요청 데이터가 누락되면 등록에 실패한다.")
    void should_FailToRegister_When_InvalidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FcmTokenRequest request = new FcmTokenRequest();  // deviceId와 fcmToken 누락

        // when
        ResultActions result = mockMvc.perform(post("/api/fcm/token")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        then(fcmService).shouldHaveNoInteractions();
    }

}
