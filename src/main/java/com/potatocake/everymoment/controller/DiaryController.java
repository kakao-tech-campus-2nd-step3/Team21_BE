package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.LocationPoint;
import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.response.CommentsResponse;
import com.potatocake.everymoment.dto.response.FriendDiariesResponse;
import com.potatocake.everymoment.dto.response.FriendDiaryResponse;
import com.potatocake.everymoment.dto.response.MemberDetailResponse;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.NotificationResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import com.potatocake.everymoment.service.DiaryService;
import com.potatocake.everymoment.service.FriendDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;
    private final FriendDiaryService friendDiaryService;
    private final CommentService commentService;

    //자동 일기 작성
    @PostMapping("/auto")
    public ResponseEntity<SuccessResponse<NotificationResponse>> createDiaryAuto(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody DiaryAutoCreateRequest diaryAutoCreateRequest) {
        Long memberId = memberDetails.getId();

        NotificationResponse response = diaryService.createDiaryAuto(memberId, diaryAutoCreateRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //수기 일기 작성
    @PostMapping("/manual")
    public ResponseEntity<SuccessResponse<Void>> createDiaryManual(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody DiaryManualCreateRequest diaryManualCreateRequest) {
        Long memberId = memberDetails.getId();
        diaryService.createDiaryManual(memberId, diaryManualCreateRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //내 일기 전체 조회(타임라인)
    @GetMapping("/my")
    public ResponseEntity<SuccessResponse<MyDiariesResponse>> getMyDiaries(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emoji,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @RequestParam(required = false) Boolean bookmark,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = memberDetails.getId();

        DiaryFilterRequest diaryFilterRequest = DiaryFilterRequest.builder()
                .keyword(keyword)
                .emoji(emoji)
                .category(category)
                .date(date)
                .from(from)
                .until(until)
                .isBookmark(bookmark)
                .key(key)
                .size(size)
                .build();

        MyDiariesResponse response = diaryService.getMyDiaries(memberId, diaryFilterRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //내 일기 상세 조회
    @GetMapping("/my/{diaryId}")
    public ResponseEntity<SuccessResponse<MyDiaryResponse>> getMyDiary(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        MyDiaryResponse response = diaryService.getMyDiary(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "특정 일기 위경도 조회", description = "특정 일기에 있는 위경도를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "위경도 조회 성공")
    @GetMapping("/{diaryId}/location")
    public ResponseEntity<SuccessResponse<LocationPoint>> getLocation(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "조회할 일기 ID", required = true)
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        LocationPoint response = diaryService.getDiaryLocation(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }


    //일기 수정
    @PatchMapping("/{diaryId}")
    public ResponseEntity<SuccessResponse<Void>> updateDiary(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId,
            @RequestBody DiaryManualCreateRequest diaryManualCreateRequest) {
        Long memberId = memberDetails.getId();

        diaryService.updateDiary(memberId, diaryId, diaryManualCreateRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //일기 삭제
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<SuccessResponse<Void>> deleteDiary(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.deleteDiary(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //북마크 설정 토글
    @PatchMapping("/{diaryId}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleBookmark(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.toggleBookmark(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //공개 설정 토글
    @PatchMapping("/{diaryId}/privacy")
    public ResponseEntity<SuccessResponse<Void>> togglePrivacy(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.togglePrivacy(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    //전체 친구 일기 조회
    @GetMapping("/friend")
    public ResponseEntity<SuccessResponse<FriendDiariesResponse>> getFriendDiaries(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emoji,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @RequestParam(required = false) Boolean bookmark,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = memberDetails.getId();

        DiaryFilterRequest diaryFilterRequest = DiaryFilterRequest.builder()
                .keyword(keyword)
                .emoji(emoji)
                .category(category)
                .date(date)
                .from(from)
                .until(until)
                .isBookmark(bookmark)
                .key(key)
                .size(size)
                .build();

        FriendDiariesResponse response = friendDiaryService.getFriendDiaries(memberId, diaryFilterRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //친구 일기 상제 조회
    @GetMapping("/friend/{diaryId}")
    public ResponseEntity<SuccessResponse<FriendDiaryResponse>> getFriendDiary(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        FriendDiaryResponse response = friendDiaryService.getFriendDiary(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //댓글 조회
    @GetMapping("/{diaryId}/comments")
    public ResponseEntity<SuccessResponse<CommentsResponse>> getComments(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "0") int key,
            @RequestParam(defaultValue = "10") int size
    ) {
        CommentsResponse response = commentService.getComments(diaryId, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    //댓글 작성
    @PostMapping("/{diaryId}/comments")
    public ResponseEntity<SuccessResponse<Void>> createComment(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long diaryId,
            @RequestBody CommentRequest commentRequest) {
        Long memberId = memberDetails.getId();

        commentService.createComment(memberId, diaryId, commentRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }
}
