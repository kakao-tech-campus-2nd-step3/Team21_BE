package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.request.FcmNotificationRequest;
import com.potatocake.everymoment.dto.response.CommentFriendResponse;
import com.potatocake.everymoment.dto.response.CommentResponse;
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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    // 댓글 목록 조회
    public CommentsResponse getComments(Long diaryId, int key, int size) {
        Pageable pageable = PageRequest.of(key, size);
        Page<Comment> commentPage = commentRepository.findAllByDiaryId(diaryId, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(this::convertToCommentResponseDTO)
                .collect(Collectors.toList());

        Integer nextKey = commentPage.hasNext() ? key + 1 : null;

        return CommentsResponse.builder()
                .comments(commentResponses)
                .next(nextKey)
                .build();
    }

    // 댓글 작성
    public void createComment(Long memberId, Long diaryId, CommentRequest commentRequest) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.isPublic()) {
            throw new GlobalException(ErrorCode.DIARY_NOT_PUBLIC);
        }

        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .member(currentMember)
                .diary(diary)
                .build();

        commentRepository.save(comment);

        // 다이어리 작성자에게 FCM 알림 전송 (자신의 댓글에는 알림 안보냄)
        if (!diary.getMember().getId().equals(memberId)) {
            fcmService.sendNotification(diary.getMember().getId(), FcmNotificationRequest.builder()
                    .title("새로운 댓글")
                    .body(currentMember.getNickname() + "님이 회원님의 일기에 댓글을 남겼습니다.")
                    .type("COMMENT")
                    .targetId(diary.getId())
                    .build());
        }
    }

    // 댓글 수정
    public void updateComment(Long memberId, Long commentId, CommentRequest commentRequest) {
        Comment comment = getExistComment(memberId, commentId);
        comment.updateContent(commentRequest.getContent());
    }

    // 댓글 삭제
    public void deleteComment(Long memberId, Long commentId) {
        Comment comment = getExistComment(memberId, commentId);
        commentRepository.delete(comment);
    }

    // 로그인한 유저가 쓴 댓글인지 확인하고, 맞을시 댓글 반환
    private Comment getExistComment(Long memberId, Long commentId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(ErrorCode.COMMENT_NOT_FOUND));

        if (!Objects.equals(currentMember.getId(), comment.getMember().getId())) {
            throw new GlobalException(ErrorCode.COMMENT_NOT_FOUND);
        }

        return comment;
    }

    // 친구 프로필 DTO 변환
    private CommentFriendResponse convertToCommentFriendResponseDTO(Member member) {
        return CommentFriendResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    // 댓글 DTO 변환
    private CommentResponse convertToCommentResponseDTO(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .commentFriendResponse(convertToCommentFriendResponseDTO(comment.getMember()))
                .content(comment.getContent())
                .createdAt(comment.getCreateAt())
                .build();
    }

}
