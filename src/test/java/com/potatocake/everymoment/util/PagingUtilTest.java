package com.potatocake.everymoment.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Member;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

class PagingUtilTest {

    private PagingUtil pagingUtil;

    @BeforeEach
    void setUp() {
        pagingUtil = new PagingUtil();
    }

    @Test
    @DisplayName("스크롤 위치가 성공적으로 생성된다.")
    void should_CreateScrollPosition_When_KeyIsNull() {
        // when
        ScrollPosition position = pagingUtil.createScrollPosition(null);

        // then
        Assertions.assertThat(position).isNotNull();
    }

    @Test
    @DisplayName("페이지 정보가 성공적으로 생성된다.")
    void should_CreatePageable_When_ValidSizeProvided() {
        // when
        Pageable pageable = pagingUtil.createPageable(10);

        // then
        assertThat(pageable).isNotNull();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("다음 페이지 키가 성공적으로 반환된다.")
    void should_ReturnNextKey_When_WindowHasNext() {
        // given
        List<Member> members = List.of(Member.builder().id(1L).build());
        Window<Member> window = Window.from(members, ScrollPosition::offset, true);

        // when
        Long nextKey = pagingUtil.getNextKey(window);

        // then
        assertThat(nextKey).isNotNull();
        assertThat(nextKey).isEqualTo(1L);
    }

    @Test
    @DisplayName("다음 페이지가 존재하지 않을 때, 키 값으로 null 을 반환한다.")
    void should_ReturnNull_When_WindowHasNoNext() {
        // given
        List<Member> members = List.of(Member.builder().id(1L).build());
        Window<Member> window = Window.from(members, ScrollPosition::offset, false);

        // when
        Long nextKey = pagingUtil.getNextKey(window);

        // then
        assertThat(nextKey).isNull();
    }

}
