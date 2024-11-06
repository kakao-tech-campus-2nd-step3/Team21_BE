package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.potatocake.everymoment.dto.response.AnonymousLoginResponse;
import com.potatocake.everymoment.dto.response.FriendRequestStatus;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MemberSearchResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.FriendRequestRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.util.JwtUtil;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private PagingUtil pagingUtil;

    @Mock
    private S3FileUploader s3FileUploader;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("익명 로그인이 성공적으로 수행된다.")
    void should_LoginAnonymously_When_ValidRequest() {
        // given
        Long nextNumber = 1234L;
        given(memberRepository.findNextAnonymousNumber()).willReturn(nextNumber);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return Member.builder()
                    .id(1L)
                    .number(member.getNumber())
                    .nickname(member.getNickname())
                    .build();
        });
        given(jwtUtil.create(anyLong())).willReturn("jwt-token");

        // when
        AnonymousLoginResponse response = memberService.anonymousLogin(null);

        // then
        assertThat(response.getNumber()).isEqualTo(nextNumber);
        assertThat(response.getToken()).isEqualTo("jwt-token");
        then(memberRepository).should().findNextAnonymousNumber();
        then(memberRepository).should().save(any(Member.class));
        then(jwtUtil).should().create(anyLong());
    }

    @Test
    @DisplayName("기존 번호로 익명 로그인하면 토큰이 발급된다.")
    void should_ReturnToken_When_ExistingNumber() {
        // given
        Long memberNumber = 1234L;
        Member member = Member.builder()
                .id(1L)
                .number(memberNumber)
                .build();

        given(memberRepository.findByNumber(memberNumber)).willReturn(Optional.of(member));
        given(jwtUtil.create(member.getId())).willReturn("jwt-token");

        // when
        AnonymousLoginResponse response = memberService.anonymousLogin(memberNumber);

        // then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getNumber()).isNull();  // 기존 회원이므로 번호를 반환하지 않음
        then(memberRepository).should().findByNumber(memberNumber);
        then(jwtUtil).should().create(member.getId());
    }

    @Test
    @DisplayName("회원 검색이 성공적으로 수행된다.")
    void should_SearchMembers_When_ValidRequest() {
        // given
        String nickname = "test";
        Long key = null;
        int size = 10;
        Long currentMemberId = 1L;

        Member member1 = Member.builder()
                .id(2L)
                .nickname("testUser1")
                .build();
        Member member2 = Member.builder()
                .id(3L)
                .nickname("testUser2")
                .build();

        ScrollPosition scrollPosition = ScrollPosition.offset();
        Window<Member> window = Window.from(List.of(member1, member2), i -> scrollPosition, false);

        given(pagingUtil.createScrollPosition(key)).willReturn(scrollPosition);
        given(pagingUtil.createPageable(size, Sort.Direction.ASC)).willReturn(PageRequest.of(0, size));
        given(memberRepository.findByNicknameContaining(anyString(), any(), any())).willReturn(window);
        given(friendRepository.existsByMemberIdAndFriendId(anyLong(), anyLong())).willReturn(false);
        given(friendRequestRepository.findBySenderIdAndReceiverId(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        // when
        MemberSearchResponse response = memberService.searchMembers(nickname, key, size, currentMemberId);

        // then
        assertThat(response.getMembers()).hasSize(2);
        assertThat(response.getMembers()).extracting("nickname")
                .containsExactly("testUser1", "testUser2");
        assertThat(response.getMembers()).extracting("friendRequestStatus")
                .containsOnly(FriendRequestStatus.NONE);
    }

    @Test
    @DisplayName("내 정보가 성공적으로 조회된다.")
    void should_ReturnMyInfo_When_ValidId() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        MemberDetailResponse response = memberService.getMyInfo(memberId);

        // then
        assertThat(response.getId()).isEqualTo(memberId);
        assertThat(response.getNickname()).isEqualTo("testUser");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("회원 정보가 성공적으로 수정된다.")
    void should_UpdateMemberInfo_When_ValidInput() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .nickname("oldNickname")
                .profileImageUrl("https://example.com/old.jpg")
                .build();

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );
        String newNickname = "newNickname";

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(s3FileUploader.uploadFile(profileImage)).willReturn("https://example.com/new.jpg");

        // when
        memberService.updateMemberInfo(memberId, profileImage, newNickname);

        // then
        assertThat(member.getNickname()).isEqualTo(newNickname);
        assertThat(member.getProfileImageUrl()).isEqualTo("https://example.com/new.jpg");
    }

    @Test
    @DisplayName("회원이 성공적으로 삭제된다.")
    void should_DeleteMember_When_ValidId() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        willDoNothing().given(memberRepository).delete(member);

        // when
        memberService.deleteMember(memberId);

        // then
        then(memberRepository).should().delete(member);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회시 예외가 발생한다.")
    void should_ThrowException_When_MemberNotFound() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMyInfo(memberId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

}
