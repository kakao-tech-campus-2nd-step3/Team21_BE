package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.FileRequest;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Files", description = "파일 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/files")
@RestController
public class FileController {

    private final FileService fileService;

    @Operation(summary = "파일 목록 조회", description = "특정 일기의 파일 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "파일 목록 조회 성공", content = @Content(schema = @Schema(implementation = FileResponse.class)))
    @GetMapping
    public ResponseEntity<SuccessResponse<FileResponse>> getFiles(@Parameter(description = "조회할 일기 ID", required = true)
                                                                  @PathVariable Long diaryId) {
        List<FileResponse> files = fileService.getFiles(diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(files));
    }

    @Operation(summary = "파일 업로드", description = "특정 일기에 파일을 업로드합니다.")
    @ApiResponse(responseCode = "200", description = "파일 업로드 성공")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> uploadFiles(
            @Parameter(description = "파일을 업로드할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "업로드할 파일 목록", required = true)
            @RequestPart List<MultipartFile> files,
            @Parameter(description = "일기에 보일 파일 이름과 순서", required = true)
            @RequestPart List<FileRequest> info
    ) {
        fileService.uploadFiles(diaryId, memberDetails.getId(), files, info);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "파일 수정", description = "특정 일기의 파일을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "파일 수정 성공")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateFiles(
            @Parameter(description = "파일을 수정할 일기 ID", required = true)
            @PathVariable Long diaryId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "수정할 파일 목록", required = true)
            @RequestPart List<MultipartFile> files,
            @Parameter(description = "일기에 보일 파일 이름과 순서", required = true)
            @RequestPart List<FileRequest> info
    ) {
        fileService.updateFiles(diaryId, memberDetails.getId(), files, info);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
