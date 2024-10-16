package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories", description = "카테고리 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "사용자의 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공", content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    @GetMapping
    public ResponseEntity<SuccessResponse<CategoryResponse>> getCategories(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails) {
        List<CategoryResponse> categories = categoryService.getCategories(memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(categories));
    }

    @Operation(summary = "카테고리 추가", description = "새로운 카테고리를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 추가 성공")
    @PostMapping
    public ResponseEntity<SuccessResponse> addCategory(
            @Parameter(description = "카테고리 생성 정보", required = true)
            @RequestBody @Valid CategoryCreateRequest request,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails) {
        categoryService.addCategory(memberDetails.getId(), request);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<SuccessResponse> updateCategory(
            @Parameter(description = "수정할 카테고리 ID", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "카테고리 수정 정보", required = true)
            @RequestBody @Valid CategoryCreateRequest request,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails) {
        categoryService.updateCategory(categoryId, memberDetails.getId(), request);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<SuccessResponse> deleteCategory(
            @Parameter(description = "삭제할 카테고리 ID", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal MemberDetails memberDetails) {
        categoryService.deleteCategory(categoryId, memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
