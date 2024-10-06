package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.MyDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.NotificationResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.entity.Notification;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryCategoryRepository diaryCategoryRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    // 자동 일기 저장 (LocationPoint, Name, Adress 만 저장)
    public NotificationResponse createDiaryAuto(Long memberId, DiaryAutoCreateRequest diaryAutoCreateRequest) {
        // member 가져옴
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Diary diary = Diary.builder()
                .member(currentMember)
                .locationPoint(diaryAutoCreateRequest.getLocationPoint().toString())
                .locationName(diaryAutoCreateRequest.getLocationName())
                .address(diaryAutoCreateRequest.getAddress())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        //알람 만듦
        String content = "현재 " + savedDiary.getLocationName() + "에 머무르고 있어요! 지금 기분은 어떠신가요?";

        Notification notification = Notification.builder()
                .member(currentMember)
                .content(content)
                .type("MOOD_CHECK")
                .targetId(savedDiary.getId())
                .build();

        //알람 저장
        Notification savedNotification = notificationRepository.save(notification);

        //알람 DTO
        NotificationResponse notificationResponse = NotificationResponse.builder()
                .content(savedNotification.getContent())
                .type(savedNotification.getType())
                .targetId(savedNotification.getTargetId())
                .createAt(savedNotification.getCreateAt())
                .build();

        return notificationResponse;
    }

    // 수동 일기 작성
    public void createDiaryManual(Long memberId, DiaryManualCreateRequest diaryManualCreateRequest) {
        // member Id
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Diary diary = Diary.builder()
                .member(currentMember)
                .content(diaryManualCreateRequest.getContent())
                .locationPoint(diaryManualCreateRequest.getLocationPoint().toString())
                .locationName(diaryManualCreateRequest.getLocationName())
                .address(diaryManualCreateRequest.getAddress())
                .emoji(diaryManualCreateRequest.getEmoji())
                .isBookmark(diaryManualCreateRequest.isBookmark())
                .isPublic(diaryManualCreateRequest.isPublic())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        Long diaryId = savedDiary.getId();
        //카테고리 저장
        //파일 저장
    }

    // 내 일기 전체 조회 (타임라인)
    @Transactional(readOnly = true)
    public MyDiariesResponse getMyDiaries(Long memberId, DiaryFilterRequest diaryFilterRequest) {
        //member 가져옴
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Page<Diary> diaryPage;

        if (diaryFilterRequest.getCategory() == null) {
            // category가 null인 경우
            Specification<Diary> spec = DiarySpecification.filterDiaries(diaryFilterRequest.getKeyword(),
                            diaryFilterRequest.getEmoji(), diaryFilterRequest.getDate(), diaryFilterRequest.getFrom(),
                            diaryFilterRequest.getUntil(), diaryFilterRequest.getBookmark())
                    .and((root, query, builder) -> builder.equal(root.get("member"), currentMember));

            diaryPage = diaryRepository.findAll(spec,
                    PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));
        } else {
            // category가 있는 경우 - DiaryCategory에서 category 같은 것 찾음
            List<DiaryCategory> diaryCategoryList = diaryCategoryRepository.findByCategoryId(
                    diaryFilterRequest.getCategory());

            // Diary 중에 memberId같은 것 가져옴
            List<Long> DiaryIdList = diaryCategoryList.stream()
                    .filter(diaryCategory -> diaryCategory.getDiary().getMember()
                            .equals(currentMember)) // memberId가 일치하는 경우 필터링
                    .map(diaryCategory -> diaryCategory.getDiary().getId())
                    .collect(Collectors.toList());

            // 가져온 DiaryId로 일기 찾음
            Specification<Diary> spec = (root, query, builder) -> root.get("id").in(DiaryIdList);
            diaryPage = diaryRepository.findAll(spec,
                    PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));
        }

        List<MyDiarySimpleResponse> diaryDTOs = diaryPage.getContent().stream()
                .map(this::convertToMyDiarySimpleResponseDto)
                .collect(Collectors.toList());

        Integer nextPage = diaryPage.hasNext() ? diaryFilterRequest.getKey() + 1 : null;

        return MyDiariesResponse.builder()
                .diaries(diaryDTOs)
                .next(nextPage)
                .build();
    }

    // 내 일기 상세 조회
    @Transactional(readOnly = true)
    public MyDiaryResponse getMyDiary(Long memberId, Long diaryId) {
        Diary diary = getExistDiary(memberId, diaryId);
        return convertToMyDiaryResponseDto(diary);
    }

    // 내 일기 수정
    public void updateDiary(Long memberId, Long diaryId, DiaryManualCreateRequest diaryManualCreateRequest) {
        Diary existingDiary = getExistDiary(memberId, diaryId);

        //카테고리 업데이트
        //파일 업데이트

        //다이어리 업데이트
        existingDiary.updateContent(diaryManualCreateRequest.getContent());
        existingDiary.updateLocationPoint(diaryManualCreateRequest.getLocationPoint() != null
                ? diaryManualCreateRequest.getLocationPoint().toString() : null);
        existingDiary.updateLocationName(diaryManualCreateRequest.getLocationName());
        existingDiary.updateAddress(diaryManualCreateRequest.getAddress());
        existingDiary.updateEmoji(diaryManualCreateRequest.getEmoji());
        existingDiary.updateBookmark(diaryManualCreateRequest.isBookmark());
        existingDiary.updatePublic(diaryManualCreateRequest.isPublic());
    }

    // 내 일기 삭제
    public void deleteDiary(Long memberId, Long diaryId) {
        Diary existingDiary = getExistDiary(memberId, diaryId);
        diaryRepository.delete(existingDiary);
    }

    // 내 일기 북마크 설정
    public void toggleBookmark(Long memberId, Long diaryId) {
        Diary existingDiary = getExistDiary(memberId, diaryId);
        existingDiary.toggleBookmark();
    }

    // 내 일기 공개 설정
    public void togglePrivacy(Long memberId, Long diaryId) {
        Diary existingDiary = getExistDiary(memberId, diaryId);
        existingDiary.togglePublic();
    }

    // 로그인한 유저의 일기가 맞는지 확인 후 일기 반환
    private Diary getExistDiary(Long memberId, Long diaryId) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        if (!Objects.equals(currentMember.getId(), diary.getMember().getId())) {
            throw new GlobalException(ErrorCode.DIARY_NOT_FOUND);
        }

        return diary;
    }

    //상세 조회시 일기DTO 변환
    private MyDiaryResponse convertToMyDiaryResponseDto(Diary savedDiary) {
        //카테고리 찾음
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(1L)
                .categoryName("일상")
                .build();
        List<CategoryResponse> categoryResponseList = new ArrayList<>();
        categoryResponseList.add(categoryResponse);

        //파일 찾음
        FileResponse fileResponse = FileResponse.builder()
                .id(1L)
                .imageUrl("image1.url")
                .order(1)
                .build();
        List<FileResponse> fileResponseList = new ArrayList<>();
        fileResponseList.add(fileResponse);

        return MyDiaryResponse.builder()
                .id(savedDiary.getId())
                .categories(categoryResponseList)
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .emoji(savedDiary.getEmoji())
                .file(fileResponseList)
                .content(savedDiary.getContent())
                .createAt(savedDiary.getCreateAt())
                .build();
    }

    //일기 전체 불러올 때, 일기DTO 변환
    private MyDiarySimpleResponse convertToMyDiarySimpleResponseDto(Diary savedDiary) {
        //파일 찾음
        ThumbnailResponse thumbnailResponse = ThumbnailResponse.builder()
                .id(1L)
                .imageUrl("image1.url")
                .build();

        return MyDiarySimpleResponse.builder()
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
