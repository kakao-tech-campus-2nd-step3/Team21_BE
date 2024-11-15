package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.service.DiarySpecification;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("일기가 성공적으로 저장된다.")
    void should_SaveDiary_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createDiary(member, "Test content", "Seoul", "Seoul, South Korea");

        // when
        Diary savedDiary = diaryRepository.save(diary);

        // then
        assertThat(savedDiary.getId()).isNotNull();
        assertThat(savedDiary.getContent()).isEqualTo("Test content");
        assertThat(savedDiary.getMember()).isEqualTo(member);
        assertThat(savedDiary.getAddress()).isEqualTo("Seoul, South Korea");
    }

    @Test
    @DisplayName("회원의 일기 목록이 성공적으로 조회된다.")
    void should_FindDiaries_When_FilteringByMember() {
        // given
        Member member = createAndSaveMember();

        Diary diary1 = createDiary(member, "Content 1", "Seoul", "Address 1");
        Diary diary2 = createDiary(member, "Content 2", "Busan", "Address 2");

        diaryRepository.saveAll(List.of(diary1, diary2));

        // when
        Specification<Diary> spec = (root, query, builder) ->
                builder.equal(root.get("member").get("id"), member.getId());
        Page<Diary> result = diaryRepository.findAll(spec, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("content")
                .containsExactly("Content 1", "Content 2");
    }

    @Test
    @DisplayName("검색 조건으로 일기가 성공적으로 필터링된다.")
    void should_FindDiaries_When_FilteringWithSearchCriteria() {
        // given
        Member member = createAndSaveMember();

        Diary diary1 = createDiary(member, "Content Seoul", "Seoul", "Seoul Address");
        diary1 = Diary.builder()
                .member(member)
                .content("Content Seoul")
                .locationPoint(diary1.getLocationPoint())
                .locationName("Seoul")
                .address("Seoul Address")
                .emoji("😊")
                .isPublic(true)
                .isBookmark(false)
                .build();

        Diary diary2 = createDiary(member, "Content Busan", "Busan", "Busan Address");
        diary2 = Diary.builder()
                .member(member)
                .content("Content Busan")
                .locationPoint(diary2.getLocationPoint())
                .locationName("Busan")
                .address("Busan Address")
                .emoji("😍")
                .isPublic(false)
                .isBookmark(false)
                .build();

        diaryRepository.saveAll(List.of(diary1, diary2));

        // when
        Specification<Diary> spec = DiarySpecification.filterDiaries(
                "Seoul",    // keyword
                List.of("😊"), // emojis
                null,      // categories
                null,      // date
                null,      // from
                null,      // until
                false,     // isBookmark
                true       // isPublic
        );

        Page<Diary> result = diaryRepository.findAll(spec, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Content Seoul");
        assertThat(result.getContent().get(0).getEmoji()).isEqualTo("😊");
    }

    @Test
    @DisplayName("북마크된 일기만 성공적으로 필터링된다.")
    void should_FindDiaries_When_FilteringBookmarked() {
        // given
        Member member = createAndSaveMember();

        Diary diary1 = createDiary(member, "Content 1", "Seoul", "Seoul Address");
        diary1 = Diary.builder()
                .member(member)
                .content("Content 1")
                .locationPoint(diary1.getLocationPoint())
                .locationName("Seoul")
                .address("Seoul Address")
                .isBookmark(true)
                .isPublic(true)
                .build();

        Diary diary2 = createDiary(member, "Content 2", "Busan", "Busan Address");
        diary2 = Diary.builder()
                .member(member)
                .content("Content 2")
                .locationPoint(diary2.getLocationPoint())
                .locationName("Busan")
                .address("Busan Address")
                .isBookmark(false)
                .isPublic(false)
                .build();

        diaryRepository.saveAll(List.of(diary1, diary2));

        // when
        Specification<Diary> spec = DiarySpecification.filterDiaries(
                null,    // keyword
                null,    // emojis
                null,    // categories
                null,    // date
                null,    // from
                null,    // until
                true,     // isBookmark
                true     // isPublic
        );

        Page<Diary> result = diaryRepository.findAll(spec, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isBookmark()).isTrue();
    }

    private Member createAndSaveMember() {
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/image.jpg")
                .build();
        return memberRepository.save(member);
    }

    private Diary createDiary(Member member, String content, String locationName, String address) {
        Point point = new GeometryFactory().createPoint(new Coordinate(37.5665, 126.978));
        return Diary.builder()
                .member(member)
                .content(content)
                .locationPoint(point)
                .locationName(locationName)
                .address(address)
                .isBookmark(false)
                .isPublic(false)
                .build();
    }

}
