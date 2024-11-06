package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    @DisplayName("댓글 내용이 성공적으로 업데이트된다.")
    void should_UpdateContent_When_NewContentProvided() {
        // given
        Comment comment = Comment.builder()
                .content("Original content")
                .build();
        String newContent = "Updated content";

        // when
        comment.updateContent(newContent);

        // then
        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("null 내용으로 업데이트하면 기존 내용이 유지된다.")
    void should_KeepOriginalContent_When_NullContentProvided() {
        // given
        Comment comment = Comment.builder()
                .content("Original content")
                .build();

        // when
        comment.updateContent(null);

        // then
        assertThat(comment.getContent()).isEqualTo("Original content");
    }

}
