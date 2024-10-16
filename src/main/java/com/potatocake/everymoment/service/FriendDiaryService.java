package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.dto.response.FriendDiariesResponse;
import com.potatocake.everymoment.dto.response.FriendDiaryResponse;
import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.LikeCountResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.LikeRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FriendDiaryService {
    private final DiaryRepository diaryRepository;
    private final DiaryCategoryRepository diaryCategoryRepository;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final LikeRepository likeRepository;

    //친구 일기 조회
    public FriendDiariesResponse getFriendDiaries(Long memberId, DiaryFilterRequest diaryFilterRequest) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        List<Friend> friends = friendRepository.findFriendsByMember(currentMember);
        List<Long> friendIdList = friends.stream()
                .map(Friend::getId)
                .collect(Collectors.toList());

        Page<Diary> diaryPage;

        // 카테고리와 이모지가 여러 개 전달될 수 있으므로 이를 리스트로 변환
        List<Long> categoryIds = diaryFilterRequest.getCategories();
        List<String> emojis = diaryFilterRequest.getEmojis();

        Specification<Diary> spec = DiarySpecification.filterDiaries(
                        diaryFilterRequest.getKeyword(),
                        emojis,
                        categoryIds,
                        diaryFilterRequest.getDate(),
                        diaryFilterRequest.getFrom(),
                        diaryFilterRequest.getUntil(),
                        diaryFilterRequest.getIsBookmark())
                .and((root, query, builder) -> root.get("member").in(friendIdList));

        diaryPage = diaryRepository.findAll(spec, PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));

        List<FriendDiarySimpleResponse> friendDiarySimpleResponseList = diaryPage.getContent().stream()
                .map(this::convertToFriendDiariesResponseDTO)
                .collect(Collectors.toList());

        Integer nextPage = diaryPage.hasNext() ? diaryFilterRequest.getKey() + 1 : null;

        return FriendDiariesResponse.builder()
                .diaries(friendDiarySimpleResponseList)
                .next(nextPage)
                .build();
    }

    // 친구 다이어리 하나 조회
    public FriendDiaryResponse getFriendDiary(Long memberId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        //글쓴사람이 친구인지 확인
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        List<Friend> friends = friendRepository.findFriendsByMember(currentMember);
        List<Long> friendIdList = friends.stream()
                .map(Friend::getId)
                .collect(Collectors.toList());

        if (!friendIdList.contains(diary.getMember())) {
            throw new GlobalException(ErrorCode.FRIEND_NOT_FOUND);
        }

        // 카테고리 찾음
        List<DiaryCategory> diaryCategories = diaryCategoryRepository.findByDiary(diary);
        List<CategoryResponse> categoryResponseList = diaryCategories.stream()
                .map(diaryCategory -> CategoryResponse.builder()
                        .id(diaryCategory.getCategory().getId())
                        .categoryName(diaryCategory.getCategory().getCategoryName())
                        .build())
                .collect(Collectors.toList());

        //like 갯수 반환
        Long likeCount = likeRepository.countByDiary(diary);

        LikeCountResponse count = LikeCountResponse.builder()
                .likeCount(likeCount)
                .build();

        FriendDiaryResponse diaryResponseDTO = FriendDiaryResponse.builder()
                .id(diary.getId())
                .categories(categoryResponseList)
                .locationName(diary.getLocationName())
                .emoji(diary.getEmoji())
                .content(diary.getContent())
                .likeCount(count)
                .createAt(diary.getCreateAt())
                .build();

        return diaryResponseDTO;
    }

    //친구 일기 DTO변환
    private FriendDiarySimpleResponse convertToFriendDiariesResponseDTO(Diary savedDiary) {
        File thumbnailFile = fileRepository.findByDiaryAndOrder(savedDiary, 1);
        ThumbnailResponse thumbnailResponse = null;
        if (thumbnailFile != null) {
            thumbnailResponse = ThumbnailResponse.builder()
                    .id(thumbnailFile.getId())
                    .imageUrl(thumbnailFile.getImageUrl())
                    .build();
        }

        return FriendDiarySimpleResponse.builder()
                .id(savedDiary.getId())
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .isPublic(savedDiary.isPublic())
                .emoji(savedDiary.getEmoji())
                .thumbnailResponse(thumbnailResponse)
                .content(savedDiary.getContent())
                .createAt(savedDiary.getCreateAt())
                .build();
    }
}
