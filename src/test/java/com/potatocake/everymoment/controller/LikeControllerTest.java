package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.LikeCountResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
@WebMvcTest(LikeController.class)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @Test
    @DisplayName("좋아요 수가 성공적으로 조회된다.")
    void should_ReturnLikeCount_When_ValidDiaryId() throws Exception {
        // given
        Long diaryId = 1L;
        LikeCountResponse response = LikeCountResponse.builder()
                .likeCount(5L)
                .build();

        given(likeService.getLikeCount(diaryId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/{diaryId}/likes", diaryId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.likeCount").value(5));

        then(likeService).should().getLikeCount(diaryId);
    }

    @Test
    @DisplayName("좋아요가 성공적으로 토글된다.")
    void should_ToggleLike_When_ValidRequest() throws Exception {
        // given
        Long diaryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(likeService).toggleLike(memberId, diaryId);

        // when
        ResultActions result = mockMvc.perform(post("/api/diaries/{diaryId}/likes", diaryId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(likeService).should().toggleLike(memberId, diaryId);
    }

}
