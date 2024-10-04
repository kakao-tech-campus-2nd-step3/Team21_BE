package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.dto.response.FriendDiariesResponse;
import com.potatocake.everymoment.dto.response.FriendDiaryResponse;
import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.security.MemberDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    //친구 일기 조회
    public FriendDiariesResponse getFriendDiaries(Long memberId, DiaryFilterRequest diaryFilterRequest) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        List<Member> friends = friendRepository.findAllFriendIdsByMemberId(currentMember);
        List<Long> friendIdList = friends.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        Page<Diary> diaryPage;

        if (diaryFilterRequest.getCategory() == null) {
            // category가 null인 경우
            Specification<Diary> spec = DiarySpecification.filterDiaries(diaryFilterRequest.getKeyword(),
                            diaryFilterRequest.getEmoji(), diaryFilterRequest.getDate(), diaryFilterRequest.getFrom(),
                            diaryFilterRequest.getUntil(), diaryFilterRequest.getBookmark())
                    .and((root, query, builder) -> root.get("memberId").in(friendIdList)); // memberIds 목록에서 검색

            diaryPage = diaryRepository.findAll(spec, PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));
        } else {
            // category가 있는 경우 - DiaryCategory에서 category 같은 것 찾음
            List<DiaryCategory> diaryCategories = diaryCategoryRepository.findByCategoryId(diaryFilterRequest.getCategory());

            // Diary중에 memberId같은 것 가져옴
            List<Long> filteredDiaryIds = diaryCategories.stream()
                    .filter(diaryCategory -> friendIdList.contains(diaryCategory.getDiary().getMember())) // memberIds 목록에서 필터링
                    .map(diaryCategory -> diaryCategory.getDiary().getId())
                    .collect(Collectors.toList());

            // 가져온 diaryId로 일기 찾음
            Specification<Diary> spec = (root, query, builder) -> root.get("id").in(filteredDiaryIds);
            diaryPage = diaryRepository.findAll(spec, PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));
        }

        List<FriendDiarySimpleResponse> friendDiarySimpleResponseList = diaryPage.getContent().stream()
                .map(this::convertToFriendDiariesResponseDTO)
                .collect(Collectors.toList());

        Integer nextPage = diaryPage.hasNext() ? diaryFilterRequest.getKey() + 1 : null;

        FriendDiariesResponse friendDiariesResponse = FriendDiariesResponse.builder()
                .diaries(friendDiarySimpleResponseList)
                .next(nextPage)
                .build();

        return friendDiariesResponse;
    }

    // 친구 다이어리 하나 조회
    public FriendDiaryResponse getFriendDiary(Long memberId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("Diary not found"));

        //글쓴사람이 친구인지 확인
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        List<Member> friends = friendRepository.findAllFriendIdsByMemberId(currentMember);
        List<Long> friendIdList = friends.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        if(!friendIdList.contains(diary.getMember())){
            throw new GlobalException(ErrorCode.FRIEND_NOT_FOUND);
        }
        //카테고리 찾음
        CategoryResponse categoryResponseDTO = CategoryResponse.builder()
                .id(1L)
                .categoryName("일상")
                .build();
        List<CategoryResponse> categoryResponseDTOList = new ArrayList<>();
        categoryResponseDTOList.add(categoryResponseDTO);

        //파일 찾음
        FileResponse fileResponse = FileResponse.builder()
                .id(1L)
                .imageUrl("image1.url")
                .order(1)
                .build();
        List<FileResponse> fileResponseDTOList = new ArrayList<>();
        fileResponseDTOList.add(fileResponse);

        //like 갯수 반환
        Integer likeCount = 11;

        FriendDiaryResponse diaryResponseDTO = FriendDiaryResponse.builder()
                .id(diary.getId())
                .categories(categoryResponseDTOList)
                .locationName(diary.getLocationName())
                .emoji(diary.getEmoji())
                .file(fileResponseDTOList)
                .content(diary.getContent())
                .likeCount(likeCount)
                .createAt(diary.getCreateAt())
                .build();

        return diaryResponseDTO;
    }

    //친구 일기 DTO변환
    private FriendDiarySimpleResponse convertToFriendDiariesResponseDTO(Diary savedDiary) {
        //파일 찾음
        ThumbnailResponse thumbnailResponse = ThumbnailResponse.builder()
                .id(1L)
                .imageUrl("image1.url")
                .build();

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
