package com.potatocake.everymoment.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public LikeCountResponse getLikeCount(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        Long likeCount = likeRepository.countByDiary(diary);

        return LikeCountResponse.builder()
                .likeCount(likeCount)
                .build();
    }

    public void toggleLike(Long memberId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Like> existingLike = likeRepository.findByMemberIdAndDiaryId(memberId, diaryId);

        if (existingLike.isPresent()) {
            // 이미 좋아요가 존재하면 삭제 (좋아요 취소)
            likeRepository.delete(existingLike.get());
        } else {
            // 좋아요가 없으면 새로 생성 (좋아요 추가)
            Like likeEntity = Like.builder()
                    .diary(diary)
                    .member(member)
                    .build();

            likeRepository.save(likeEntity);
        }
    }

}
