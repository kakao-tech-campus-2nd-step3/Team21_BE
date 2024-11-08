package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.FriendRequestPageRequest;
import com.potatocake.everymoment.dto.response.FriendRequestResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendRequestService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FriendRequestController.class)
class FriendRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendRequestService friendRequestService;

    @Test
    @DisplayName("친구 요청 목록이 성공적으로 조회된다.")
    void should_ReturnFriendRequests_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FriendRequestPageRequest response = FriendRequestPageRequest.builder()
                .friendRequests(List.of(
                        FriendRequestResponse.builder()
                                .id(1L)
                                .senderId(2L)
                                .nickname("requester")
                                .profileImageUrl("https://example.com/profile.jpg")
                                .build()
                ))
                .next(null)
                .build();

        given(friendRequestService.getFriendRequests(null, 10, memberId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/friend-requests")
                .with(user(memberDetails))
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.friendRequests[0].nickname").value("requester"));

        then(friendRequestService).should().getFriendRequests(null, 10, memberId);
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 전송된다.")
    void should_SendFriendRequest_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Long targetMemberId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(friendRequestService).sendFriendRequest(memberId, targetMemberId);

        // when
        ResultActions result = mockMvc.perform(post("/api/members/{memberId}/friend-requests", targetMemberId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(friendRequestService).should().sendFriendRequest(memberId, targetMemberId);
    }

    @Test
    @DisplayName("자신에게 친구 요청을 보내면 실패한다.")
    void should_FailToSendRequest_When_RequestingSelf() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        // when
        ResultActions result = mockMvc.perform(post("/api/members/{memberId}/friend-requests", memberId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(ErrorCode.SELF_FRIEND_REQUEST.getMessage()));

        then(friendRequestService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 수락된다.")
    void should_AcceptFriendRequest_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Long requestId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(friendRequestService).acceptFriendRequest(requestId, memberId);

        // when
        ResultActions result = mockMvc.perform(post("/api/friend-requests/{requestId}/accept", requestId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(friendRequestService).should().acceptFriendRequest(requestId, memberId);
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 거절된다.")
    void should_RejectFriendRequest_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Long requestId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(friendRequestService).rejectFriendRequest(requestId, memberId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/friend-requests/{requestId}/reject", requestId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(friendRequestService).should().rejectFriendRequest(requestId, memberId);
    }

}
