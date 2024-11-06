package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.FriendProfileResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FriendService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FriendController.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendService friendService;

    @Test
    @DisplayName("특정 친구의 일기 목록이 성공적으로 조회된다.")
    void should_ReturnFriendDiaries_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Long friendId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        OneFriendDiariesResponse response = OneFriendDiariesResponse.builder()
                .diaries(List.of(
                        FriendDiarySimpleResponse.builder()
                                .id(1L)
                                .content("Test diary")
                                .locationName("Test location")
                                .build()
                ))
                .next(null)
                .build();

        given(friendService.OneFriendDiariesResponse(memberId, friendId, null, 0, 10))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/friends/{friendId}/diaries", friendId)
                .with(user(memberDetails))
                .param("key", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.diaries[0].content").value("Test diary"));

        then(friendService).should().OneFriendDiariesResponse(memberId, friendId, null, 0, 10);
    }

    @Test
    @DisplayName("내 친구 목록이 성공적으로 조회된다.")
    void should_ReturnFriendList_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FriendListResponse response = FriendListResponse.builder()
                .friends(List.of(
                        FriendProfileResponse.builder()
                                .id(2L)
                                .nickname("friend")
                                .profileImageUrl("https://example.com/profile.jpg")
                                .build()
                ))
                .next(null)
                .build();

        given(friendService.getFriendList(memberId, null, 0, 10))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/friends/friends")
                .with(user(memberDetails))
                .param("key", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.friends[0].nickname").value("friend"));

        then(friendService).should().getFriendList(memberId, null, 0, 10);
    }

    @Test
    @DisplayName("친구가 성공적으로 삭제된다.")
    void should_DeleteFriend_When_ValidId() throws Exception {
        // given
        Long memberId = 1L;
        Long friendId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(friendService).deleteFriend(memberId, friendId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/friends/{friendId}", friendId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(friendService).should().deleteFriend(memberId, friendId);
    }

}
