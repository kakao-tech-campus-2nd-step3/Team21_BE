package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CategoryRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    public void addCategory(Long memberId, CategoryCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        categoryRepository.save(request.toEntity(member));
    }

    public List<CategoryResponse> getCategories(Long memberId) {
        return categoryRepository.findByMemberId(memberId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public void updateCategory(Long categoryId, Long memberId, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CATEGORY_NOT_FOUND));

        category.checkOwner(memberId);

        category.update(request.getCategoryName());
    }

    public void deleteCategory(Long categoryId, Long memberId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CATEGORY_NOT_FOUND));

        category.checkOwner(memberId);

        categoryRepository.delete(category);
    }

}
