package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileTest {

    @DisplayName("파일이 성공적으로 생성된다.")
    @Test
    void should_CreateFile_When_ValidInput() {
        // given
        Long id = 1L;
        Diary diary = mock(Diary.class);
        String imageUrl = "https://example.com/image.jpg";
        Integer order = 1;

        // when
        File file = File.builder()
                .id(id)
                .diary(diary)
                .imageUrl(imageUrl)
                .order(order)
                .build();

        // then
        assertThat(file.getId()).isEqualTo(id);
        assertThat(file.getDiary()).isEqualTo(diary);
        assertThat(file.getImageUrl()).isEqualTo(imageUrl);
        assertThat(file.getOrder()).isEqualTo(order);
    }

}
