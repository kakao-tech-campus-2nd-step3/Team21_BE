package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.potatocake.everymoment.dto.response.FriendRequestStatus;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.FriendRequestRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.util.PagingUtil;
import com.potatocake.everymoment.util.S3FileUploader;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private PagingUtil pagingUtil;

    @Mock
    private S3FileUploader s3FileUploader;

    @Test
    @DisplayName("회원 목록 검색이 성공적으로 수행된다.")
    void should_ReturnMemberList_When_ValidSearchConditions() {
        // given
        String nickname = "testUser";
        Long key = 1L;
        int size = 10;
        Long currentMemberId = 1L;

        Member member1 = Member.builder().id(2L).nickname("testUser1").build();
        Member member2 = Member.builder().id(3L).nickname("testUser2").build();
        List<Member> members = List.of(member1, member2);
        Window<Member> window = Window.from(members, ScrollPosition::offset, false);

        given(memberRepository.findByNicknameContaining(anyString(), any(), any()))
                .willReturn(window);
        given(friendRepository.existsByMemberIdAndFriendId(anyLong(), anyLong()))
                .willReturn(false);
        given(friendRequestRepository.findBySenderIdAndReceiverId(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        given(pagingUtil.getNextKey(any(), any())).willReturn(null);

        // when
        MemberSearchResponse result = memberService.searchMembers(nickname, key, size, currentMemberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(2);
        assertThat(result.getMembers().get(0).getFriendRequestStatus()).isEqualTo(FriendRequestStatus.NONE);
        assertThat(result.getMembers().get(1).getFriendRequestStatus()).isEqualTo(FriendRequestStatus.NONE);
        assertThat(result.getNext()).isNull();

        then(memberRepository).should().findByNicknameContaining(anyString(), any(), any());
        then(friendRepository).should(times(2)).existsByMemberIdAndFriendId(anyLong(), anyLong());
        then(friendRequestRepository).should(times(4)).findBySenderIdAndReceiverId(anyLong(), anyLong());
        then(pagingUtil).should().getNextKey(any(), any());
    }

    @Test
    @DisplayName("내 정보 조회가 성공적으로 수행된다.")
    void should_ReturnMyInfo_When_ValidMemberId() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().build();
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        MemberDetailResponse result = memberService.getMyInfo(memberId);

        // then
        assertThat(result).isNotNull();

        then(memberRepository).should().findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정이 성공적으로 수행된다.")
    void should_UpdateMemberInfo_When_ValidInput() {
        // given
        Long memberId = 1L;
        MultipartFile profileImage = mock(MultipartFile.class);
        String nickname = "newNickname";

        Member member = Member.builder().build();
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(s3FileUploader.uploadFile(profileImage)).willReturn("profileUrl");

        // when
        memberService.updateMemberInfo(memberId, profileImage, nickname);

        // then
        assertThat(member.getNickname()).isEqualTo("newNickname");

        then(memberRepository).should().findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 정보를 수정하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_MemberNotFound() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(GlobalException.class)
                .isThrownBy(() -> memberService.updateMemberInfo(memberId, null, "newNickname"));
    }

}
