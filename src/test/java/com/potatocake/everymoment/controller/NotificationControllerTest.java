package com.potatocake.everymoment.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.NotificationListResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록이 성공적으로 조회된다.")
    void should_GetNotifications_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        List<NotificationListResponse> responses = List.of(
                NotificationListResponse.builder()
                        .id(1L)
                        .content("Notification 1")
                        .type("TEST1")
                        .targetId(1L)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build(),
                NotificationListResponse.builder()
                        .id(2L)
                        .content("Notification 2")
                        .type("TEST2")
                        .targetId(2L)
                        .isRead(true)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        given(notificationService.getNotifications(memberId)).willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/api/notifications")
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info").isArray())
                .andExpect(jsonPath("$.info[0].content").value("Notification 1"))
                .andExpect(jsonPath("$.info[0].read").value(false))
                .andExpect(jsonPath("$.info[1].content").value("Notification 2"))
                .andExpect(jsonPath("$.info[1].read").value(true));

        then(notificationService).should().getNotifications(memberId);
    }

    @Test
    @DisplayName("알림이 성공적으로 읽음 처리된다.")
    void should_UpdateNotification_When_ValidId() throws Exception {
        // given
        Long memberId = 1L;
        Long notificationId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(notificationService).updateNotification(memberId, notificationId);

        // when
        ResultActions result = mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(notificationService).should().updateNotification(eq(memberId), eq(notificationId));
    }

}
