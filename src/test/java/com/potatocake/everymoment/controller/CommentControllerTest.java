package com.potatocake.everymoment.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("댓글이 성공적으로 수정된다.")
    void should_UpdateComment_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Long commentId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CommentRequest request = new CommentRequest();
        request.setContent("Updated comment");

        willDoNothing().given(commentService).updateComment(
                eq(memberId),
                eq(commentId),
                argThat(req -> req.getContent().equals("Updated comment"))
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(commentService).should().updateComment(
                eq(memberId),
                eq(commentId),
                argThat(req -> req.getContent().equals("Updated comment"))
        );
    }

    @Test
    @DisplayName("댓글이 성공적으로 삭제된다.")
    void should_DeleteComment_When_ValidId() throws Exception {
        // given
        Long memberId = 1L;
        Long commentId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(commentService).deleteComment(memberId, commentId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(commentService).should().deleteComment(memberId, commentId);
    }

    @Test
    @DisplayName("댓글 내용이 누락되면 수정에 실패한다.")
    void should_FailToUpdate_When_ContentMissing() throws Exception {
        // given
        Long memberId = 1L;
        Long commentId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CommentRequest request = new CommentRequest();
        // content 누락

        // when
        ResultActions result = mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        then(commentService).shouldHaveNoInteractions();
    }

}
