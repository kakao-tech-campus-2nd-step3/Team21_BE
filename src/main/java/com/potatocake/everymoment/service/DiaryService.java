package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.DiaryAutoRequestDTO;
import com.potatocake.everymoment.dto.request.DiaryManualRequestDTO;
import com.potatocake.everymoment.dto.response.CategoryResponseDTO;
import com.potatocake.everymoment.dto.response.FileResponseDTO;
import com.potatocake.everymoment.dto.response.MyDiariesResponseDTO;
import com.potatocake.everymoment.dto.response.MyDiaryResponseDTO;
import com.potatocake.everymoment.dto.response.MyDiarySimpleResponseDTO;
import com.potatocake.everymoment.dto.response.NotificationResponseDTO;
import com.potatocake.everymoment.dto.response.ThumbnailResponseDTO;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.Notification;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.NotificationRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryCategoryRepository diaryCategoryRepository;
    private final NotificationRepository notificationRepository;

    public DiaryService(DiaryRepository diaryRepository, DiaryCategoryRepository diaryCategoryRepository,
                        NotificationRepository notificationRepository) {
        this.diaryRepository = diaryRepository;
        this.diaryCategoryRepository = diaryCategoryRepository;
        this.notificationRepository = notificationRepository;
    }

    // 자동 일기 저장 (LocationPoint, Name, Adress 만 저장)
    public SuccessResponse<NotificationResponseDTO> createDiaryAuto(DiaryAutoRequestDTO diaryAutoRequestDTO) {
        // member Id 가져옴
        Long memberId = 1L;

        Diary diary = Diary.builder()
                .memberId(memberId)
                .locationPoint(diaryAutoRequestDTO.getLocationPoint().toString())
                .locationName(diaryAutoRequestDTO.getLocationName())
                .address(diaryAutoRequestDTO.getAddress())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        //알람 만듦
        String content = "현재 " + savedDiary.getLocationName() + "에 머무르고 있어요! 지금 기분은 어떠신가요?";

        Notification notification = Notification.builder()
                .memberId(memberId)
                .content(content)
                .type("MOOD_CHECK")
                .targetId(savedDiary.getId())
                .build();

        //알람 저장
        Notification savedNotification = notificationRepository.save(notification);

        //알람 DTO
        NotificationResponseDTO notificationResponseDTO = NotificationResponseDTO.builder()
                .content(savedNotification.getContent())
                .type(savedNotification.getType())
                .targetId(savedNotification.getTargetId())
                .createAt(savedNotification.getCreateAt())
                .build();

        return SuccessResponse.<NotificationResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(notificationResponseDTO)
                .build();
    }

    // 수동 일기 작성
    public SuccessResponse<?> createDiaryManual(DiaryManualRequestDTO diaryManualRequestDTO) {
        // member Id 가져옴
        Long memberId = 1L;

        Diary diary = Diary.builder()
                .memberId(memberId)
                .content(diaryManualRequestDTO.getContent())
                .locationPoint(diaryManualRequestDTO.getLocationPoint().toString())
                .locationName(diaryManualRequestDTO.getLocationName())
                .address(diaryManualRequestDTO.getAddress())
                .emoji(diaryManualRequestDTO.getEmoji())
                .isBookmark(diaryManualRequestDTO.isBookmark())
                .isPublic(diaryManualRequestDTO.isPublic())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        Long diaryId = savedDiary.getId();
        //카테고리 저장
        //파일 저장

        return SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .build();
    }

    // 내 일기 전체 조회 (타임라인)
    public SuccessResponse<MyDiariesResponseDTO> getMyDiaries(String keyword, String emoji, Long category,
                                                              LocalDate date, LocalDate from, LocalDate until,
                                                              Boolean isBookmark, int key, int size) {
        //member id 가져옴
        Long memberId = 1L;

        Page<Diary> diaryPage;

        if (category == null) {
            // category가 null인 경우
            Specification<Diary> spec = DiarySpecification.filterDiaries(keyword, emoji, date, from, until, isBookmark)
                    .and((root, query, builder) -> builder.equal(root.get("memberId"), memberId));

            diaryPage = diaryRepository.findAll(spec, PageRequest.of(key, size));
        } else {
            // category가 있는 경우 - DiaryCategory에서 category 같은 것 찾음
            List<DiaryCategory> diaryCategoryList = diaryCategoryRepository.findByCategoryId(category);

            // Diary 중에 memberId같은 것 가져옴
            List<Long> DiaryIdList = diaryCategoryList.stream()
                    .filter(diaryCategory -> diaryCategory.getDiary().getMemberId()
                            .equals(memberId)) // memberId가 일치하는 경우 필터링
                    .map(diaryCategory -> diaryCategory.getDiary().getId())
                    .collect(Collectors.toList());

            // 가져온 DiaryId로 일기 찾음
            Specification<Diary> spec = (root, query, builder) -> root.get("id").in(DiaryIdList);
            diaryPage = diaryRepository.findAll(spec, PageRequest.of(key, size));
        }

        List<MyDiarySimpleResponseDTO> diaryDTOs = diaryPage.getContent().stream()
                .map(this::convertToMyDiarySimpleResponseDto)
                .collect(Collectors.toList());

        Integer nextPage = diaryPage.hasNext() ? key + 1 : null;

        MyDiariesResponseDTO myDiariesResponseDTO = MyDiariesResponseDTO.builder()
                .diaries(diaryDTOs)
                .key(nextPage)
                .build();

        return SuccessResponse.<MyDiariesResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(myDiariesResponseDTO)
                .build();
    }

    // 내 일기 상세 조회
    public SuccessResponse<MyDiaryResponseDTO> getMyDiary(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diary not found"));

        return SuccessResponse.<MyDiaryResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(convertToMyDiaryResponseDto(diary))
                .build();
    }

    //상세 조회시 일기DTO 변환
    private MyDiaryResponseDTO convertToMyDiaryResponseDto(Diary savedDiary) {
        //카테고리 찾음
        CategoryResponseDTO categoryResponseDTO = CategoryResponseDTO.builder()
                .id(1L)
                .categoryName("일상")
                .build();
        List<CategoryResponseDTO> categoryResponseDTOList = new ArrayList<>();
        categoryResponseDTOList.add(categoryResponseDTO);

        //파일 찾음
        FileResponseDTO fileResponseDTO = FileResponseDTO.builder()
                .id(1L)
                .imageUrl("image1.url")
                .order(1)
                .build();
        List<FileResponseDTO> fileResponseDTOList = new ArrayList<>();
        fileResponseDTOList.add(fileResponseDTO);

        return MyDiaryResponseDTO.builder()
                .id(savedDiary.getId())
                .categories(categoryResponseDTOList)
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .emoji(savedDiary.getEmoji())
                .file(fileResponseDTOList)
                .content(savedDiary.getContent())
                .createAt(savedDiary.getCreateAt())
                .build();
    }

    //일기 전체 불러올 때, 일기DTO 변환
    private MyDiarySimpleResponseDTO convertToMyDiarySimpleResponseDto(Diary savedDiary) {
        //파일 찾음
        ThumbnailResponseDTO thumbnailResponseDTO = ThumbnailResponseDTO.builder()
                .id(1L)
                .imageUrl("image1.url")
                .build();

        return MyDiarySimpleResponseDTO.builder()
                .id(savedDiary.getId())
                .address(savedDiary.getAddress())
                .locationName(savedDiary.getLocationName())
                .isBookmark(savedDiary.isBookmark())
                .isPublic(savedDiary.isPublic())
                .emoji(savedDiary.getEmoji())
                .thumbnailResponseDTO(thumbnailResponseDTO)
                .content(savedDiary.getContent())
                .createAt(savedDiary.getCreateAt())
                .build();
    }
}
