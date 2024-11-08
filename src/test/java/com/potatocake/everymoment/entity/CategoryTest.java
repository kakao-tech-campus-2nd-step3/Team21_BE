package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    @DisplayName("카테고리명이 성공적으로 업데이트된다.")
    void should_UpdateName_When_NewNameProvided() {
        // given
        Category category = Category.builder()
                .categoryName("Original Name")
                .build();

        // when
        category.update("New Name");

        // then
        assertThat(category.getCategoryName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("소유자 확인이 성공적으로 수행된다.")
    void should_CheckOwner_When_VerifyingOwnership() {
        // given
        Long ownerId = 1L;
        Member owner = Member.builder()
                .id(ownerId)
                .build();

        Category category = Category.builder()
                .member(owner)
                .build();

        // when & then
        assertThatCode(() -> category.checkOwner(ownerId))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> category.checkOwner(2L))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_OWNER);
    }

}
