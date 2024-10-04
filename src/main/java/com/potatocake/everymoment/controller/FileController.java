package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.FileRequest;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FileService;
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

@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/files")
@RestController
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<SuccessResponse<FileResponse>> getFiles(@PathVariable Long diaryId) {
        List<FileResponse> files = fileService.getFiles(diaryId);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(files));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> uploadFiles(@PathVariable Long diaryId,
                                                       @AuthenticationPrincipal MemberDetails memberDetails,
                                                       @RequestPart List<MultipartFile> files,
                                                       @RequestPart List<FileRequest> info) {
        fileService.uploadFiles(diaryId, memberDetails.getId(), files, info);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateFiles(@PathVariable Long diaryId,
                                                       @AuthenticationPrincipal MemberDetails memberDetails,
                                                       @RequestPart List<MultipartFile> files,
                                                       @RequestPart List<FileRequest> info) {
        fileService.updateFiles(diaryId, memberDetails.getId(), files, info);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
