package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.DiaryAutoRequestDTO;
import com.potatocake.everymoment.dto.request.DiaryManualRequestDTO;
import com.potatocake.everymoment.dto.response.NotificationResponseDTO;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Notification;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.NotificationRepository;
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
}
