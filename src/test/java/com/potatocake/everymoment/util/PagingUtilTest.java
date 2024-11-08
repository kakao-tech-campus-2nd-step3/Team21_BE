package com.potatocake.everymoment.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Window;

class PagingUtilTest {

    private PagingUtil pagingUtil;

    @BeforeEach
    void setUp() {
        pagingUtil = new PagingUtil();
    }

    @Test
    @DisplayName("스크롤 위치가 성공적으로 생성된다.")
    void should_CreateScrollPosition_When_GivenKey() {
        // given
        Long key = 1L;

        // when
        ScrollPosition position = pagingUtil.createScrollPosition(key);

        // then
        assertThat(position).isNotNull();
    }

    @Test
    @DisplayName("key 가 null 일 때 offset 스크롤 위치가 생성된다.")
    void should_CreateOffsetPosition_When_KeyIsNull() {
        // given
        Long key = null;

        // when
        ScrollPosition position = pagingUtil.createScrollPosition(key);

        // then
        assertThat(position).isEqualTo(ScrollPosition.offset());
    }

    @Test
    @DisplayName("페이지 정보가 성공적으로 생성된다.")
    void should_CreatePageable_When_ValidInput() {
        // given
        int size = 10;
        Direction direction = Direction.DESC;

        // when
        Pageable pageable = pagingUtil.createPageable(size, direction);

        // then
        assertThat(pageable.getPageSize()).isEqualTo(size);
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(direction);
    }

    @Test
    @DisplayName("페이지 정보가 올바른 정렬 순서를 가진다.")
    void should_CreatePageableWithCorrectSort_When_DirectionGiven() {
        // given
        int size = 10;
        Direction direction = Direction.ASC;

        // when
        Pageable pageable = pagingUtil.createPageable(size, direction);

        // then
        Sort sort = pageable.getSort();
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(direction);
    }

    @Test
    @DisplayName("다음 키가 성공적으로 생성된다.")
    void should_GetNextKey_When_ValidWindow() {
        // given
        TestEntity entity1 = new TestEntity(1L);
        TestEntity entity2 = new TestEntity(2L);

        List<TestEntity> content = List.of(entity1, entity2);
        ScrollPosition scrollPosition = ScrollPosition.offset();

        Window<TestEntity> window = Window.from(content, i -> scrollPosition, true);

        // when
        Long nextKey = pagingUtil.getNextKey(window, TestEntity::getId);

        // then
        assertThat(nextKey).isEqualTo(2L);
    }

    @Test
    @DisplayName("다음 페이지가 없으면 null 을 반환한다.")
    void should_ReturnNull_When_NoNextPage() {
        // given
        TestEntity entity = new TestEntity(1L);
        ScrollPosition scrollPosition = ScrollPosition.offset();

        Window<TestEntity> window = Window.from(List.of(entity), i -> scrollPosition, false);

        // when
        Long nextKey = pagingUtil.getNextKey(window, TestEntity::getId);

        // then
        assertThat(nextKey).isNull();
    }

    @Test
    @DisplayName("빈 윈도우에 대해 null 을 반환한다.")
    void should_ReturnNull_When_EmptyWindow() {
        // given
        ScrollPosition scrollPosition = ScrollPosition.offset();

        Window<TestEntity> window = Window.from(List.of(), i -> scrollPosition, false);

        // when
        Long nextKey = pagingUtil.getNextKey(window, TestEntity::getId);

        // then
        assertThat(nextKey).isNull();
    }

    private static class TestEntity {
        private final Long id;

        TestEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

}
