package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.LocationPoint;
import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryPatchRequest;
import com.potatocake.everymoment.dto.response.CommentsResponse;
import com.potatocake.everymoment.dto.response.FriendDiariesResponse;
import com.potatocake.everymoment.dto.response.FriendDiaryResponse;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.NotificationResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import com.potatocake.everymoment.service.DiaryService;
import com.potatocake.everymoment.service.FriendDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

@Tag(name = "Diaries", description = "일기 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;
    private final FriendDiaryService friendDiaryService;
    private final CommentService commentService;

    @Operation(summary = "자동 일기 작성", description = "자동으로 일기를 작성합니다.")
    @ApiResponse(responseCode = "200", description = "자동 일기 작성 성공", content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
    @PostMapping("/auto")
    public ResponseEntity<SuccessResponse> createDiaryAuto(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "자동 일기 작성 정보", required = true)
            @RequestBody DiaryAutoCreateRequest diaryAutoCreateRequest) {
        Long memberId = memberDetails.getId();

        diaryService.createDiaryAuto(memberId, diaryAutoCreateRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "수기 일기 작성", description = "수동으로 일기를 작성합니다.")
    @ApiResponse(responseCode = "200", description = "수기 일기 작성 성공")
    @PostMapping("/manual")
    public ResponseEntity<SuccessResponse<Void>> createDiaryManual(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "수기 일기 작성 정보", required = true)
            @RequestBody DiaryManualCreateRequest diaryManualCreateRequest) {
        Long memberId = memberDetails.getId();
        diaryService.createDiaryManual(memberId, diaryManualCreateRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "내 일기 전체 조회", description = "사용자의 모든 일기를 조회합니다. (타임라인)")
    @ApiResponse(responseCode = "200", description = "내 일기 전체 조회 성공", content = @Content(schema = @Schema(implementation = MyDiariesResponse.class)))
    @GetMapping("/my")
    public ResponseEntity<SuccessResponse<MyDiariesResponse>> getMyDiaries(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "이모지 필터")
            @RequestParam(required = false) String emoji,
            @Parameter(description = "카테고리")
            @RequestParam(required = false) String category,
            @Parameter(description = "특정 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시작 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "종료 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @Parameter(description = "북마크 여부")
            @RequestParam(required = false) Boolean bookmark,
            @Parameter(description = "페이지 키")
            @RequestParam(defaultValue = "0") int key,
            @Parameter(description = "페이지 크기")
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

    @Operation(summary = "내 일기 상세 조회", description = "특정 일기의 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 일기 상세 조회 성공", content = @Content(schema = @Schema(implementation = MyDiaryResponse.class)))
    @GetMapping("/my/{diaryId}")
    public ResponseEntity<SuccessResponse<MyDiaryResponse>> getMyDiary(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "조회할 일기 ID", required = true)
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

    @Operation(summary = "일기 수정", description = "기존 일기를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "일기 수정 성공")
    @PatchMapping("/{diaryId}")
    public ResponseEntity<SuccessResponse<Void>> updateDiary(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "수정할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "일기 수정 정보", required = true)
            @RequestBody DiaryPatchRequest diaryPatchRequest) {
        Long memberId = memberDetails.getId();

        diaryService.updateDiary(memberId, diaryId, diaryPatchRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "일기 삭제", description = "일기를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "일기 삭제 성공")
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<SuccessResponse<Void>> deleteDiary(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "삭제할 일기 ID", required = true)
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.deleteDiary(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "북마크 설정 토글", description = "일기의 북마크 상태를 토글합니다.")
    @ApiResponse(responseCode = "200", description = "북마크 설정 토글 성공")
    @PatchMapping("/{diaryId}/bookmark")
    public ResponseEntity<SuccessResponse<Void>> toggleBookmark(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "토글할 일기 ID", required = true)
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.toggleBookmark(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "공개 설정 토글", description = "일기의 공개 상태를 토글합니다.")
    @ApiResponse(responseCode = "200", description = "공개 설정 토글 성공")
    @PatchMapping("/{diaryId}/privacy")
    public ResponseEntity<SuccessResponse<Void>> togglePrivacy(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "토글할 일기 ID", required = true)
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        diaryService.togglePrivacy(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "전체 친구 일기 조회", description = "사용자 친구들의 모든 일기를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 일기 전체 조회 성공", content = @Content(schema = @Schema(implementation = FriendDiariesResponse.class)))
    @GetMapping("/friend")
    public ResponseEntity<SuccessResponse<FriendDiariesResponse>> getFriendDiaries(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "이모지 필터")
            @RequestParam(required = false) String emoji,
            @Parameter(description = "카테고리")
            @RequestParam(required = false) String category,
            @Parameter(description = "특정 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시작 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "종료 날짜")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @Parameter(description = "북마크 여부")
            @RequestParam(required = false) Boolean bookmark,
            @Parameter(description = "페이지 키")
            @RequestParam(defaultValue = "0") int key,
            @Parameter(description = "페이지 크기")
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

    @Operation(summary = "친구 일기 상세 조회", description = "특정 친구 일기의 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 일기 상세 조회 성공", content = @Content(schema = @Schema(implementation = FriendDiaryResponse.class)))
    @GetMapping("/friend/{diaryId}")
    public ResponseEntity<SuccessResponse<FriendDiaryResponse>> getFriendDiary(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "조회할 친구 일기 ID", required = true)
            @PathVariable Long diaryId) {
        Long memberId = memberDetails.getId();

        FriendDiaryResponse response = friendDiaryService.getFriendDiary(memberId, diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "댓글 조회", description = "특정 일기의 댓글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 조회 성공", content = @Content(schema = @Schema(implementation = CommentsResponse.class)))
    @GetMapping("/{diaryId}/comments")
    public ResponseEntity<SuccessResponse<CommentsResponse>> getComments(
            @Parameter(description = "댓글을 조회할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "페이지 키")
            @RequestParam(defaultValue = "0") int key,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        CommentsResponse response = commentService.getComments(diaryId, key, size);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(response));
    }

    @Operation(summary = "댓글 작성", description = "특정 일기에 댓글을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    @PostMapping("/{diaryId}/comments")
    public ResponseEntity<SuccessResponse<Void>> createComment(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "댓글을 작성할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "댓글 작성 정보", required = true)
            @RequestBody @Valid CommentRequest commentRequest) {
        Long memberId = memberDetails.getId();

        commentService.createComment(memberId, diaryId, commentRequest);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
