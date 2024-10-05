package com.potatocake.everymoment.controller;

import static com.potatocake.everymoment.exception.ErrorCode.INFO_REQUIRED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

@WithMockUser
@AutoConfigureMockMvc
@SpringBootTest
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원 목록 검색이 성공적으로 수행된다.")
    void should_SearchMembers_When_ValidInput() throws Exception {
        // given
        String nickname = "testUser";
        Long key = 1L;
        int size = 10;
        MemberSearchResponse response = MemberSearchResponse.builder().build();

        given(memberService.searchMembers(nickname, key, size)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members")
                .param("nickname", nickname)
                .param("key", key.toString())
                .param("size", String.valueOf(size)));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(memberService).should().searchMembers(nickname, key, size);
    }

    @Test
    @DisplayName("내 정보 조회가 성공적으로 수행된다.")
    void should_ReturnMyInfo_When_ValidMember() throws Exception {
        // given
        Long memberId = 1L;
        MemberDetails memberDetails = createMemberDetails(memberId, 1234L, "test");

        MemberDetailResponse response = MemberDetailResponse.builder().build();
        given(memberService.getMyInfo(memberId)).willReturn(response);

        // when
        ResultActions result = performGet("/api/members/me", memberDetails);

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info").isNotEmpty());

        then(memberService).should().getMyInfo(memberId);
    }

    @Test
    @DisplayName("회원 ID로 정보 조회가 성공적으로 수행된다.")
    void should_ReturnMemberInfo_When_ValidMemberId() throws Exception {
        // given
        Long memberId = 1L;
        MemberResponse response = MemberResponse.builder().build();

        given(memberService.getMemberInfo(memberId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/members/{memberId}", memberId));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info").isNotEmpty());

        then(memberService).should().getMemberInfo(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정이 성공적으로 수행된다.")
    void should_UpdateMemberInfo_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        MemberDetails memberDetails = createMemberDetails(memberId, 1234L, "test");

        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "image.png", "image/png", new byte[]{});
        String nickname = "newNickname";

        willDoNothing().given(memberService).updateMemberInfo(anyLong(), any(MultipartFile.class), anyString());

        // when
        ResultActions result = performMultipart("/api/members", profileImage, nickname, memberDetails);

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(memberService).should().updateMemberInfo(memberId, profileImage, nickname);
    }

    @Test
    @DisplayName("프로필 이미지와 닉네임이 모두 누락되면 예외가 발생한다.")
    void should_ThrowException_When_ProfileImageAndNicknameAreMissing() throws Exception {
        // when
        ResultActions result = mockMvc.perform(multipart("/api/members"));

        // then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INFO_REQUIRED.getStatus().value()))
                .andExpect(jsonPath("$.message").value(INFO_REQUIRED.getMessage()));

        then(memberService).shouldHaveNoInteractions();
    }

    private Member createMember(Long memberId, Long number, String nickname) {
        return Member.builder()
                .id(memberId)
                .number(number)
                .nickname(nickname)
                .build();
    }

    private MemberDetails createMemberDetails(Long memberId, Long number, String nickname) {
        Member member = createMember(memberId, number, nickname);
        return new MemberDetails(member);
    }

    private ResultActions performGet(String url, MemberDetails memberDetails) throws Exception {
        return mockMvc.perform(get(url)
                .with(user(memberDetails)));
    }

    private ResultActions performMultipart(String url, MockMultipartFile file, String nickname,
                                           MemberDetails memberDetails) throws Exception {
        return mockMvc.perform(multipart(url)
                .file(file)
                .param("nickname", nickname)
                .with(user(memberDetails)));
    }

}
