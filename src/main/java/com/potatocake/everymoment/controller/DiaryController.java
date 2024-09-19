package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.DiaryAutoRequestDTO;
import com.potatocake.everymoment.dto.request.DiaryManualRequestDTO;
import com.potatocake.everymoment.dto.response.NotificationResponseDTO;
import com.potatocake.everymoment.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<SuccessResponse<NotificationResponseDTO>> createDiaryAuto(
            @RequestBody DiaryAutoRequestDTO diaryAutoRequestDTO) {
        SuccessResponse<NotificationResponseDTO> response = diaryService.createDiaryAuto(diaryAutoRequestDTO);
        return ResponseEntity.ok(response);
    }

    //수기 일기 작성
    @PostMapping("/manual")
    public ResponseEntity<SuccessResponse<?>> createDiaryManual(
            @RequestBody DiaryManualRequestDTO diaryManualRequestDTO) {
        SuccessResponse<?> response = diaryService.createDiaryManual(diaryManualRequestDTO);
        return ResponseEntity.ok(response);
    }
}
