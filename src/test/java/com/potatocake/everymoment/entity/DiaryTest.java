package com.potatocake.everymoment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

class DiaryTest {

    @Test
    @DisplayName("북마크 상태가 성공적으로 토글된다.")
    void should_ToggleBookmark_When_Called() {
        // given
        Diary diary = Diary.builder()
                .isBookmark(false)
                .build();

        // when
        diary.toggleBookmark();

        // then
        assertThat(diary.isBookmark()).isTrue();

        // when
        diary.toggleBookmark();

        // then
        assertThat(diary.isBookmark()).isFalse();
    }

    @Test
    @DisplayName("공개 상태가 성공적으로 토글된다.")
    void should_TogglePrivacy_When_Called() {
        // given
        Diary diary = Diary.builder()
                .isPublic(false)
                .build();

        // when
        diary.togglePublic();

        // then
        assertThat(diary.isPublic()).isTrue();

        // when
        diary.togglePublic();

        // then
        assertThat(diary.isPublic()).isFalse();
    }

    @Test
    @DisplayName("일기 내용이 성공적으로 업데이트된다.")
    void should_UpdateContent_When_NewContentProvided() {
        // given
        Diary diary = Diary.builder()
                .content("Original content")
                .build();
        String newContent = "Updated content";

        // when
        diary.updateContent(newContent);

        // then
        assertThat(diary.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("location 정보가 성공적으로 업데이트된다.")
    void should_UpdateLocation_When_NewLocationProvided() {
        // given
        Point originalPoint = mock(Point.class);
        Diary diary = Diary.builder()
                .locationPoint(originalPoint)
                .locationName("Original location")
                .address("Original address")
                .build();

        Point newPoint = mock(Point.class);
        String newLocationName = "New location";
        String newAddress = "New address";

        // when
        diary.updateLocationPoint(newPoint);
        diary.updateLocationName(newLocationName);
        diary.updateAddress(newAddress);

        // then
        assertThat(diary.getLocationPoint()).isEqualTo(newPoint);
        assertThat(diary.getLocationName()).isEqualTo(newLocationName);
        assertThat(diary.getAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("이모지가 성공적으로 업데이트된다.")
    void should_UpdateEmoji_When_NewEmojiProvided() {
        // given
        Diary diary = Diary.builder()
                .emoji("😊")
                .build();
        String newEmoji = "😍";

        // when
        diary.updateEmoji(newEmoji);

        // then
        assertThat(diary.getEmoji()).isEqualTo(newEmoji);
    }

    @Test
    @DisplayName("작성자 확인이 성공적으로 수행된다.")
    void should_CheckOwner_When_VerifyingOwnership() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Diary diary = Diary.builder()
                .member(member)
                .build();

        // when & then
        assertThat(diary.checkOwner(memberId)).isTrue();
        assertThat(diary.checkOwner(2L)).isFalse();
    }

    @Test
    @DisplayName("내용이 성공적으로 null로 업데이트된다.")
    void should_UpdateContentNull_When_Called() {
        // given
        Diary diary = Diary.builder()
                .content("Original content")
                .build();

        // when
        diary.updateContentNull();

        // then
        assertThat(diary.getContent()).isNull();
    }

    @Test
    @DisplayName("이모지가 성공적으로 null로 업데이트된다.")
    void should_UpdateEmojiNull_When_Called() {
        // given
        Diary diary = Diary.builder()
                .emoji("😊")
                .build();

        // when
        diary.updateEmojiNull();

        // then
        assertThat(diary.getEmoji()).isNull();
    }

}
