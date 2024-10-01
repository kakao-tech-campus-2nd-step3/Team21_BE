package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/files")
@RestController
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<SuccessResponse<FileResponse>> getFiles(@PathVariable Long diaryId,
                                                                  @AuthenticationPrincipal MemberDetails memberDetails) {
        List<FileResponse> files = fileService.getFiles(diaryId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(files));
    }

}
