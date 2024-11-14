package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.response.LikeCountResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Like;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.LikeRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("좋아요 수가 성공적으로 조회된다.")
    void should_ReturnLikeCount_When_ValidDiaryId() {
        // given
        Long diaryId = 1L;
        Diary diary = Diary.builder()
                .id(diaryId)
                .build();

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(likeRepository.countByDiary(diary)).willReturn(5L);

        // when
        LikeCountResponse response = likeService.getLikeCount(diaryId);

        // then
        assertThat(response.getLikeCount()).isEqualTo(5L);
        then(diaryRepository).should().findById(diaryId);
        then(likeRepository).should().countByDiary(diary);
    }

    @Test
    @DisplayName("좋아요가 성공적으로 추가된다.")
    void should_AddLike_When_NotLiked() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .nickname("testUser")
                .build();
        Member diaryOwner = Member.builder()
                .id(2L)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(diaryOwner)
                .isPublic(true)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(likeRepository.findByMemberIdAndDiaryIdWithLock(memberId, diaryId))
                .willReturn(Optional.empty());

        // when
        likeService.toggleLike(memberId, diaryId);

        // then
        then(likeRepository).should().save(any(Like.class));
        then(notificationService).should().createAndSendNotification(
                eq(diaryOwner.getId()),
                eq(NotificationType.LIKE),
                eq(diaryId),
                eq(member.getNickname())
        );
    }

    @Test
    @DisplayName("좋아요가 성공적으로 취소된다.")
    void should_RemoveLike_When_AlreadyLiked() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .isPublic(true)
                .build();
        Like like = Like.builder()
                .id(1L)
                .member(member)
                .diary(diary)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(likeRepository.findByMemberIdAndDiaryIdWithLock(memberId, diaryId))
                .willReturn(Optional.of(like));

        // when
        likeService.toggleLike(memberId, diaryId);

        // then
        then(likeRepository).should().delete(like);
    }

    @Test
    @DisplayName("다른 사용자의 비공개 일기에 좋아요를 누르면 예외가 발생한다.")
    void should_ThrowException_When_DiaryNotPublic() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        
        Diary diary = Diary.builder()
                .id(diaryId)
                .isPublic(false)
                .member(Member.builder().id(2L).build())  // 다른 사용자의 일기
                .build();

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when & then
        assertThatThrownBy(() -> likeService.toggleLike(memberId, diaryId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIARY_NOT_PUBLIC);
    }

}
