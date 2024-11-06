package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.potatocake.everymoment.dto.request.CategoryCreateRequest;
import com.potatocake.everymoment.dto.response.CategoryResponse;
import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CategoryRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("카테고리가 성공적으로 추가된다.")
    void should_AddCategory_When_ValidInput() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        CategoryCreateRequest request = new CategoryCreateRequest("New Category");

        Category category = Category.builder()
                .id(1L)
                .member(member)
                .categoryName("New Category")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        categoryService.addCategory(memberId, request);

        // then
        then(memberRepository).should().findById(memberId);
        then(categoryRepository).should().save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 목록이 성공적으로 조회된다.")
    void should_GetCategories_When_ValidMemberId() {
        // given
        Long memberId = 1L;
        List<Category> categories = List.of(
                Category.builder().id(1L).categoryName("Category 1").build(),
                Category.builder().id(2L).categoryName("Category 2").build()
        );

        given(categoryRepository.findByMemberId(memberId)).willReturn(categories);

        // when
        List<CategoryResponse> responses = categoryService.getCategories(memberId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("categoryName")
                .containsExactly("Category 1", "Category 2");
        then(categoryRepository).should().findByMemberId(memberId);
    }

    @Test
    @DisplayName("카테고리가 성공적으로 수정된다.")
    void should_UpdateCategory_When_ValidInput() {
        // given
        Long categoryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .member(member)
                .categoryName("Original Name")
                .build();

        CategoryCreateRequest request = new CategoryCreateRequest("Updated Name");

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.updateCategory(categoryId, memberId, request);

        // then
        assertThat(category.getCategoryName()).isEqualTo("Updated Name");
        then(categoryRepository).should().findById(categoryId);
    }

    @Test
    @DisplayName("다른 사용자의 카테고리를 수정하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_UpdateOtherUserCategory() {
        // given
        Long categoryId = 1L;
        Long memberId = 1L;
        Member owner = Member.builder()
                .id(2L)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .member(owner)
                .categoryName("Original Name")
                .build();

        CategoryCreateRequest request = new CategoryCreateRequest("Updated Name");

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_OWNER);
    }

    @Test
    @DisplayName("카테고리가 성공적으로 삭제된다.")
    void should_DeleteCategory_When_ValidRequest() {
        // given
        Long categoryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .member(member)
                .categoryName("Category")
                .build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId, memberId);

        // then
        then(categoryRepository).should().findById(categoryId);
        then(categoryRepository).should().delete(category);
    }

}
