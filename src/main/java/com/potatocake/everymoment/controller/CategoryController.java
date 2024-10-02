package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<CategoryResponse>> getCategories(
            @AuthenticationPrincipal MemberDetails memberDetails) {
        List<CategoryResponse> categories = categoryService.getCategories(memberDetails.getId());

        return ResponseEntity.ok()
                .body(SuccessResponse.ok(categories));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> addCategory(@RequestBody @Valid CategoryCreateRequest request,
                                                       @AuthenticationPrincipal MemberDetails memberDetails) {
        categoryService.addCategory(memberDetails.getId(), request);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
