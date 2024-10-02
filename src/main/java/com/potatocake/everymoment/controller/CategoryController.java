package com.potatocake.everymoment.controller;

import com.potatocake.everymoment.dto.SuccessResponse;
import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<SuccessResponse> addCategory(@RequestBody @Valid CategoryCreateRequest request,
                                                       @AuthenticationPrincipal MemberDetails memberDetails) {
        categoryService.addCategory(memberDetails.getId(), request);

        return ResponseEntity.ok()
                .body(SuccessResponse.ok());
    }

}
