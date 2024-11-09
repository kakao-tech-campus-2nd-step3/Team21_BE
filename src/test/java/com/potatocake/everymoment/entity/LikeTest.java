package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LikeTest {

    @Test
    @DisplayName("좋아요가 성공적으로 생성된다.")
    void should_CreateLike_When_ValidInput() {
        // given
        Member member = Member.builder()
                .id(1L)
                .nickname("user")
                .build();
        Diary diary = Diary.builder()
                .id(1L)
                .content("Test diary")
                .build();

        // when
        Like like = Like.builder()
                .member(member)
                .diary(diary)
                .build();

        // then
        assertThat(like.getMember()).isEqualTo(member);
        assertThat(like.getDiary()).isEqualTo(diary);
    }

}
