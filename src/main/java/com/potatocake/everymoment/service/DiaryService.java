package com.potatocake.everymoment.service;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.LocationPoint;
import com.potatocake.everymoment.dto.request.CategoryRequest;
import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryPatchRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.MyDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.ThumbnailResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CategoryRepository;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.repository.LikeRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryCategoryRepository diaryCategoryRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final FileRepository fileRepository;
    private final LikeRepository likeRepository;
    private final GeometryFactory geometryFactory;
    private final NotificationService notificationService;

    // 자동 일기 저장 (LocationPoint, Name, Adress 만 저장)
    public void createDiaryAuto(Long memberId, DiaryAutoCreateRequest diaryAutoCreateRequest) {
        // member 가져옴
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        double longitude = diaryAutoCreateRequest.getLocationPoint().getLongitude();
        double latitude = diaryAutoCreateRequest.getLocationPoint().getLatitude();

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Diary diary = Diary.builder()
                .member(currentMember)
                .locationPoint(point)
                .locationName(diaryAutoCreateRequest.getLocationName())
                .address(diaryAutoCreateRequest.getAddress())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        notificationService.createAndSendNotification(
                memberId,
                NotificationType.MOOD_CHECK,
                savedDiary.getId(),
                savedDiary.getLocationName()
        );
    }

    // 수동 일기 작성
    public void createDiaryManual(Long memberId, DiaryManualCreateRequest diaryManualCreateRequest) {
        // member Id
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        double longitude = diaryManualCreateRequest.getLocationPoint().getLongitude();
        double latitude = diaryManualCreateRequest.getLocationPoint().getLatitude();

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Diary diary = Diary.builder()
                .member(currentMember)
                .content(diaryManualCreateRequest.getContent())
                .diaryDate(diaryManualCreateRequest.getDiaryDate())
                .locationPoint(point)
                .locationName(diaryManualCreateRequest.getLocationName())
                .address(diaryManualCreateRequest.getAddress())
                .emoji(diaryManualCreateRequest.getEmoji())
                .isBookmark(diaryManualCreateRequest.isBookmark())
                .isPublic(diaryManualCreateRequest.isPublic())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        //카테고리 저장
        List<CategoryRequest> categoryRequestList = diaryManualCreateRequest.getCategories();
        if (categoryRequestList != null) {
            addDiaryCategory(savedDiary, currentMember.getId(), categoryRequestList);
        }
    }

    // 내 일기 전체 조회 (타임라인)
    @Transactional(readOnly = true)
    public MyDiariesResponse getMyDiaries(Long memberId, DiaryFilterRequest diaryFilterRequest) {
        // member 가져옴
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Page<Diary> diaryPage;

        List<String> categories = diaryFilterRequest.getCategories();
        List<String> emojis = diaryFilterRequest.getEmojis();

        Specification<Diary> spec;

        if (!diaryFilterRequest.hasFilter()) {
            LocalDate today = LocalDate.now();

            spec = DiarySpecification.filterDiaries(
                            diaryFilterRequest.getKeyword(),
                            emojis,
                            categories,
                            today,
                            diaryFilterRequest.getFrom(),
                            diaryFilterRequest.getUntil(),
                            diaryFilterRequest.getIsBookmark(),
                            diaryFilterRequest.getIsPublic())
                    .and((root, query, builder) -> builder.equal(root.get("member"), currentMember));

            diaryPage = diaryRepository.findAll(spec,
                    PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize()));
        } else {
            spec = DiarySpecification.filterDiaries(
                            diaryFilterRequest.getKeyword(),
                            emojis,
                            categories,
                            diaryFilterRequest.getDate(),
                            diaryFilterRequest.getFrom(),
                            diaryFilterRequest.getUntil(),
                            diaryFilterRequest.getIsBookmark(),
                            diaryFilterRequest.getIsPublic())
                    .and((root, query, builder) -> builder.equal(root.get("member"), currentMember));

            diaryPage = diaryRepository.findAll(spec,
                    PageRequest.of(diaryFilterRequest.getKey(), diaryFilterRequest.getSize(),
                            Sort.by(Sort.Direction.DESC, "createAt")));
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
        return convertToMyDiaryResponseDto(diary, memberId);
    }

    // 내 일기 위치 조회
    public LocationPoint getDiaryLocation(Long memberId, Long diaryId) {
        Diary diary = getExistDiary(memberId, diaryId);
        Point point = diary.getLocationPoint();

        return LocationPoint.builder()
                .latitude(point.getX())
                .longitude(point.getY())
                .build();
    }

    // 내 일기 수정
    public void updateDiary(Long memberId, Long diaryId, DiaryPatchRequest diaryPatchRequest) {
        Diary existingDiary = getExistDiary(memberId, diaryId);

        // 카테고리 업데이트
        if (diaryPatchRequest.getDeleteAllCategories() != null && diaryPatchRequest.getDeleteAllCategories()) {
            diaryCategoryRepository.deleteByDiary(existingDiary);
        } else {
            List<CategoryRequest> categoryRequestList = diaryPatchRequest.getCategories();

            if (categoryRequestList != null && !categoryRequestList.isEmpty()) {
                diaryCategoryRepository.deleteByDiary(existingDiary);
                addDiaryCategory(existingDiary, memberId, categoryRequestList);
            }
        }

        // 다이어리 업데이트
        if (diaryPatchRequest.getContentDelete() != null && diaryPatchRequest.getContentDelete()) {
            existingDiary.updateContentNull();
        } else {
            existingDiary.updateContent(diaryPatchRequest.getContent());
        }

        existingDiary.updateLocationName(diaryPatchRequest.getLocationName());
        existingDiary.updateAddress(diaryPatchRequest.getAddress());

        if (diaryPatchRequest.getEmojiDelete() != null && diaryPatchRequest.getEmojiDelete()) {
            existingDiary.updateEmojiNull();
        } else {
            existingDiary.updateEmoji(diaryPatchRequest.getEmoji());
        }
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

    //다이어리에 카테고리 추가
    private void addDiaryCategory(Diary savedDiary, Long memberId, List<CategoryRequest> categoryRequestList) {
        for (CategoryRequest categoryRequest : categoryRequestList) {
            Long categoryId = categoryRequest.getCategoryId();

            DiaryCategory diaryCategory = DiaryCategory.builder()
                    .diary(savedDiary)
                    .category(categoryRepository.findById(categoryId)
                            .map(category -> {
                                category.checkOwner(memberId);
                                return category;
                            })
                            .orElseThrow(() -> new GlobalException(ErrorCode.CATEGORY_NOT_FOUND)))
                    .build();

            diaryCategoryRepository.save(diaryCategory);
        }
    }

    //상세 조회시 일기DTO 변환
    private MyDiaryResponse convertToMyDiaryResponseDto(Diary savedDiary, Long memberId) {
        // 카테고리 찾음
        List<DiaryCategory> diaryCategories = diaryCategoryRepository.findByDiary(savedDiary);
        List<CategoryResponse> categoryResponseList = diaryCategories.stream()
                .map(diaryCategory -> CategoryResponse.builder()
                        .id(diaryCategory.getCategory().getId())
                        .categoryName(diaryCategory.getCategory().getCategoryName())
                        .build())
                .collect(Collectors.toList());

        boolean isLiked = likeRepository.existsByMemberIdAndDiaryId(memberId, savedDiary.getId());

        return MyDiaryResponse.builder()
                .id(savedDiary.getId())
                .categories(categoryResponseList)
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .emoji(savedDiary.getEmoji())
                .content(savedDiary.getContent())
                .isLiked(isLiked)
                .createAt(savedDiary.getCreateAt())
                .diaryDate(savedDiary.getDiaryDate())
                .build();
    }

    //일기 전체 불러올 때, 일기DTO 변환
    private MyDiarySimpleResponse convertToMyDiarySimpleResponseDto(Diary savedDiary) {
        File thumbnailFile = fileRepository.findByDiaryAndOrder(savedDiary, 1);
        ThumbnailResponse thumbnailResponse = null;
        if (thumbnailFile != null) {
            thumbnailResponse = ThumbnailResponse.builder()
                    .id(thumbnailFile.getId())
                    .imageUrl(thumbnailFile.getImageUrl())
                    .build();
        }

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
                .diaryDate(savedDiary.getDiaryDate())
                .build();
    }
}
