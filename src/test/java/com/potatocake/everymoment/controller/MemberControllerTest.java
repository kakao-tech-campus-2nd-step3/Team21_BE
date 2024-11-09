package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.response.AnonymousLoginResponse;
import com.potatocake.everymoment.dto.response.FriendRequestStatus;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberMyResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResultResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("익명 로그인이 성공적으로 수행된다.")
    void should_LoginAnonymously_When_ValidRequest() throws Exception {
        // given
        Long memberNumber = null;
        AnonymousLoginResponse response = AnonymousLoginResponse.builder()
                .number(1234L)
                .token("jwt-token")
                .build();

        given(memberService.anonymousLogin(memberNumber)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/anonymous-login"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.number").value(1234))
                .andExpect(jsonPath("$.info.token").value("jwt-token"));

        then(memberService).should().anonymousLogin(memberNumber);
    }

    @Test
    @DisplayName("회원 검색이 성공적으로 수행된다.")
    void should_SearchMembers_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MemberSearchResponse response = MemberSearchResponse.builder()
                .members(List.of(
                        MemberSearchResultResponse.builder()
                                .id(2L)
                                .nickname("searchResult")
                                .profileImageUrl("https://example.com/profile.jpg")
                                .friendRequestStatus(FriendRequestStatus.NONE)
                                .build()
                ))
                .next(null)
                .build();

        given(memberService.searchMembers(null, null, 10, memberId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members")
                .with(user(memberDetails))
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.members[0].nickname").value("searchResult"));

        then(memberService).should().searchMembers(null, null, 10, memberId);
    }

    @Test
    @DisplayName("내 정보가 성공적으로 조회된다.")
    void should_ReturnMyInfo_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MemberDetailResponse response = MemberDetailResponse.builder()
                .id(memberId)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        given(memberService.getMyInfo(memberId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/me")
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.nickname").value("testUser"));

        then(memberService).should().getMyInfo(memberId);
    }

    @Test
    @DisplayName("회원 정보가 성공적으로 조회된다.")
    void should_ReturnMemberInfo_When_ValidId() throws Exception {
        // given
        Long targetMemberId = 2L;
        MemberMyResponse response = MemberMyResponse.builder()
                .id(targetMemberId)
                .nickname("otherUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        given(memberService.getMemberInfo(targetMemberId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/{memberId}", targetMemberId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.nickname").value("otherUser"));

        then(memberService).should().getMemberInfo(targetMemberId);
    }

    @Test
    @DisplayName("회원 정보가 성공적으로 수정된다.")
    void should_UpdateMemberInfo_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        String nickname = "newNickname";

        willDoNothing().given(memberService)
                .updateMemberInfo(memberId, profileImage, nickname);

        // when
        ResultActions result = mockMvc.perform(multipart("/api/members")
                .file(profileImage)
                .param("nickname", nickname)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(memberService).should().updateMemberInfo(memberId, profileImage, nickname);
    }

    @Test
    @DisplayName("회원이 성공적으로 삭제된다.")
    void should_DeleteMember_When_ValidRequest() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(memberService).deleteMember(memberId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/members")
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(memberService).should().deleteMember(memberId);
    }

    @Test
    @DisplayName("정보가 누락되면 회원 정보 수정에 실패한다.")
    void should_FailToUpdate_When_InfoMissing() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        // when
        ResultActions result = mockMvc.perform(multipart("/api/members")
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(ErrorCode.INFO_REQUIRED.getMessage()));

        then(memberService).shouldHaveNoInteractions();
    }

}
