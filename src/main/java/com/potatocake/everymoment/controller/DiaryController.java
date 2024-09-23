package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.DiaryAutoRequest;
import com.potatocake.everymoment.dto.request.DiaryManualRequest;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.NotificationResponse;
import com.potatocake.everymoment.service.DiaryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    //자동 일기 작성
    @PostMapping("/auto")
    public ResponseEntity<SuccessResponse<NotificationResponse>> createDiaryAuto(
            @RequestBody DiaryAutoRequest diaryAutoRequest) {
        NotificationResponse notificationResponse = diaryService.createDiaryAuto(diaryAutoRequest);
        SuccessResponse<NotificationResponse> response = SuccessResponse.<NotificationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(notificationResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    //수기 일기 작성
    @PostMapping("/manual")
    public ResponseEntity<SuccessResponse<Void>> createDiaryManual(
            @RequestBody DiaryManualRequest diaryManualRequest) {
        diaryService.createDiaryManual(diaryManualRequest);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();

        return ResponseEntity.ok(response);
    }

    //내 일기 전체 조회(타임라인)
    @GetMapping("my")
    public ResponseEntity<SuccessResponse<MyDiariesResponse>> getMyDiaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emoji,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @RequestParam(required = false) Boolean bookmark,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size
    ) {
        MyDiariesResponse myDiariesResponse = diaryService.getMyDiaries(keyword, emoji, category, date, from, until,
                bookmark, key, size);
        SuccessResponse<MyDiariesResponse> response = SuccessResponse.<MyDiariesResponse>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(myDiariesResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    //내 일기 상세 조회
    @GetMapping("/my/{id}")
    public ResponseEntity<SuccessResponse<MyDiaryResponse>> getMyDiary(@PathVariable Long id) {
        MyDiaryResponse myDiaryResponse = diaryService.getMyDiary(id);
        SuccessResponse<MyDiaryResponse> response = SuccessResponse.<MyDiaryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(myDiaryResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    //일기 수정
    @PatchMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> updateDiary(@PathVariable Long id,
                                                             @RequestBody DiaryManualRequest diaryManualRequest) {
        diaryService.updateDiary(id, diaryManualRequest);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }

    //일기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }

    //북마크 설정 토글
    @PatchMapping("/{id}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleBookmark(@PathVariable Long id) {
        diaryService.toggleBookmark(id);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }

    //공개 설정 토글
    @PatchMapping("/{id}/privacy")
    public ResponseEntity<SuccessResponse<Void>> togglePrivacy(@PathVariable Long id) {
        diaryService.togglePrivacy(id);
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .info(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
