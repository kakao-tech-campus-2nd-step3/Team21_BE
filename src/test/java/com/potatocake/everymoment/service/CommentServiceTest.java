package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.response.CommentsResponse;
import com.potatocake.everymoment.entity.Comment;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CommentRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("댓글 목록이 성공적으로 조회된다.")
    void should_GetComments_When_ValidDiaryId() {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .nickname("testUser")
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .member(member)
                .content("Test comment")
                .build();

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        given(commentRepository.findAllByDiaryId(eq(diaryId), any(PageRequest.class)))
                .willReturn(commentPage);

        // when
        CommentsResponse response = commentService.getComments(diaryId, 0, 10);

        // then
        assertThat(response.getComments()).hasSize(1);
        assertThat(response.getComments().get(0).getContent()).isEqualTo("Test comment");
        then(commentRepository).should().findAllByDiaryId(eq(diaryId), any(PageRequest.class));
    }

    @Test
    @DisplayName("댓글이 성공적으로 생성된다.")
    void should_CreateComment_When_ValidInput() {
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

        CommentRequest request = new CommentRequest();
        request.setContent("New comment");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(commentRepository.save(any(Comment.class))).willReturn(
                Comment.builder()
                        .id(1L)
                        .member(member)
                        .diary(diary)
                        .content(request.getContent())
                        .build()
        );

        // when
        commentService.createComment(memberId, diaryId, request);

        // then
        then(commentRepository).should().save(any(Comment.class));
        then(notificationService).should().createAndSendNotification(
                eq(diary.getMember().getId()),
                eq(NotificationType.COMMENT),
                eq(diaryId),
                eq(member.getNickname())
        );
    }

    @Test
    @DisplayName("자신의 일기에 댓글을 작성할 때는 알림이 발송되지 않는다.")
    void should_NotSendNotification_When_CommentingOwnDiary() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .nickname("testUser")
                .build();

        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)  // 일기 작성자와 댓글 작성자가 동일
                .isPublic(true)
                .build();

        CommentRequest request = new CommentRequest();
        request.setContent("New comment");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(commentRepository.save(any(Comment.class))).willReturn(
                Comment.builder()
                        .id(1L)
                        .member(member)
                        .diary(diary)
                        .content(request.getContent())
                        .build()
        );

        // when
        commentService.createComment(memberId, diaryId, request);

        // then
        then(commentRepository).should().save(any(Comment.class));
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비공개 일기에 댓글을 작성하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_DiaryNotPublic() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .isPublic(false)
                .build();
        CommentRequest request = new CommentRequest();
        request.setContent("New comment");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(memberId, diaryId, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIARY_NOT_PUBLIC);
    }

    @Test
    @DisplayName("댓글이 성공적으로 수정된다.")
    void should_UpdateComment_When_ValidInput() {
        // given
        Long memberId = 1L;
        Long commentId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Comment comment = Comment.builder()
                .id(commentId)
                .member(member)
                .content("Original content")
                .build();
        CommentRequest request = new CommentRequest();
        request.setContent("Updated content");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.updateComment(memberId, commentId, request);

        // then
        assertThat(comment.getContent()).isEqualTo("Updated content");
    }

}
