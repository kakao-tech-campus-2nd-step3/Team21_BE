package com.potatocake.everymoment.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록이 성공적으로 조회된다.")
    void should_ReturnCategories_When_RequestCategories() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        List<CategoryResponse> responses = List.of(
                CategoryResponse.builder()
                        .id(1L)
                        .categoryName("Category 1")
                        .build(),
                CategoryResponse.builder()
                        .id(2L)
                        .categoryName("Category 2")
                        .build()
        );

        given(categoryService.getCategories(memberId)).willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/api/categories")
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info").isArray())
                .andExpect(jsonPath("$.info[0].categoryName").value("Category 1"))
                .andExpect(jsonPath("$.info[1].categoryName").value("Category 2"));

        then(categoryService).should().getCategories(memberId);
    }

    @Test
    @DisplayName("카테고리가 성공적으로 추가된다.")
    void should_CreateCategory_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CategoryCreateRequest request = new CategoryCreateRequest("New Category");

        willDoNothing().given(categoryService).addCategory(
                eq(memberId),
                argThat(req -> req.getCategoryName().equals("New Category"))
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/categories")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(categoryService).should().addCategory(
                eq(memberId),
                argThat(req -> req.getCategoryName().equals("New Category"))
        );
    }

    @Test
    @DisplayName("카테고리가 성공적으로 수정된다.")
    void should_UpdateCategory_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Long categoryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CategoryCreateRequest request = new CategoryCreateRequest("Updated Category");

        willDoNothing().given(categoryService).updateCategory(
                eq(categoryId),
                eq(memberId),
                argThat(req -> req.getCategoryName().equals("Updated Category"))
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/categories/{categoryId}", categoryId)
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(categoryService).should().updateCategory(
                eq(categoryId),
                eq(memberId),
                argThat(req -> req.getCategoryName().equals("Updated Category"))
        );
    }

    @Test
    @DisplayName("카테고리가 성공적으로 삭제된다.")
    void should_DeleteCategory_When_ValidId() throws Exception {
        // given
        Long memberId = 1L;
        Long categoryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(categoryService).deleteCategory(categoryId, memberId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/categories/{categoryId}", categoryId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(categoryService).should().deleteCategory(categoryId, memberId);
    }

    @Test
    @DisplayName("카테고리명이 누락되면 생성에 실패한다.")
    void should_FailToCreate_When_CategoryNameMissing() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CategoryCreateRequest request = new CategoryCreateRequest();  // categoryName 누락

        // when
        ResultActions result = mockMvc.perform(post("/api/categories")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        then(categoryService).shouldHaveNoInteractions();
    }

}
