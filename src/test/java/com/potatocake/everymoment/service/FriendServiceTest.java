package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.potatocake.everymoment.dto.response.FriendListResponse;
import com.potatocake.everymoment.dto.response.OneFriendDiariesResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private FileRepository fileRepository;

    @Test
    @DisplayName("특정 친구의 일기 목록이 성공적으로 조회된다.")
    void should_ReturnFriendDiaries_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Long friendId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member friend = Member.builder()
                .id(friendId)
                .build();
        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();

        Diary diary = Diary.builder()
                .id(1L)
                .member(friend)
                .content("Test diary")
                .isPublic(true)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberRepository.findById(friendId)).willReturn(Optional.of(friend));
        given(friendRepository.findByMemberAndFriend(member, friend))
                .willReturn(Optional.of(friendship));
        given(diaryRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(diary)));

        // when
        OneFriendDiariesResponse response = friendService
                .OneFriendDiariesResponse(memberId, friendId, null, 0, 10);

        // then
        assertThat(response.getDiaries()).hasSize(1);
        assertThat(response.getDiaries().get(0).getContent()).isEqualTo("Test diary");

        then(memberRepository).should().findById(memberId);
        then(memberRepository).should().findById(friendId);
        then(friendRepository).should().findByMemberAndFriend(member, friend);
        then(diaryRepository).should().findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("친구 목록이 성공적으로 조회된다.")
    void should_ReturnFriendList_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member friend = Member.builder()
                .id(2L)
                .nickname("friend")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        Friend friendship = Friend.builder()
                .member(member)
                .friend(friend)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(friendRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(friendship)));

        // when
        FriendListResponse response = friendService.getFriendList(memberId, null, 0, 10);

        // then
        assertThat(response.getFriends()).hasSize(1);
        assertThat(response.getFriends().get(0).getNickname()).isEqualTo("friend");

        then(memberRepository).should().findById(memberId);
        then(friendRepository).should().findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("친구가 성공적으로 삭제된다.")
    void should_DeleteFriend_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Long friendId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member friend = Member.builder()
                .id(friendId)
                .build();
        Friend friendship1 = Friend.builder()
                .member(member)
                .friend(friend)
                .build();
        Friend friendship2 = Friend.builder()
                .member(friend)
                .friend(member)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberRepository.findById(friendId)).willReturn(Optional.of(friend));
        given(friendRepository.findByMemberAndFriend(member, friend))
                .willReturn(Optional.of(friendship1));
        given(friendRepository.findByMemberAndFriend(friend, member))
                .willReturn(Optional.of(friendship2));

        // when
        friendService.deleteFriend(memberId, friendId);

        // then
        then(friendRepository).should().delete(friendship1);
        then(friendRepository).should().delete(friendship2);
    }

    @Test
    @DisplayName("존재하지 않는 친구를 삭제하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_FriendNotFound() {
        // given
        Long memberId = 1L;
        Long friendId = 2L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member friend = Member.builder()
                .id(friendId)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberRepository.findById(friendId)).willReturn(Optional.of(friend));
        given(friendRepository.findByMemberAndFriend(member, friend))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> friendService.deleteFriend(memberId, friendId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FRIEND_NOT_FOUND);
    }

}
