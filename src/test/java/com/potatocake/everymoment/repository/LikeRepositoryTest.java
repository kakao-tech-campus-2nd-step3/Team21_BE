package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Like;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Test
    @DisplayName("좋아요가 성공적으로 저장된다.")
    void should_SaveLike_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Like like = Like.builder()
                .member(member)
                .diary(diary)
                .build();

        // when
        Like savedLike = likeRepository.save(like);

        // then
        assertThat(savedLike.getId()).isNotNull();
        assertThat(savedLike.getMember()).isEqualTo(member);
        assertThat(savedLike.getDiary()).isEqualTo(diary);
    }

    @Test
    @DisplayName("일기의 좋아요 수가 성공적으로 조회된다.")
    void should_CountLikes_When_CountingByDiary() {
        // given
        Member member1 = createAndSaveMember();
        Member member2 = Member.builder()
                .number(5678L)
                .nickname("testUser2")
                .profileImageUrl("https://example.com/profile2.jpg")
                .build();
        memberRepository.save(member2);

        Diary diary = createAndSaveDiary(member1);

        Like like1 = Like.builder()
                .member(member1)
                .diary(diary)
                .build();
        Like like2 = Like.builder()
                .member(member2)
                .diary(diary)
                .build();

        likeRepository.saveAll(List.of(like1, like2));

        // when
        Long likeCount = likeRepository.countByDiary(diary);

        // then
        assertThat(likeCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("특정 사용자의 좋아요가 성공적으로 조회된다.")
    void should_FindLike_When_FilteringByMemberAndDiary() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Like like = Like.builder()
                .member(member)
                .diary(diary)
                .build();
        likeRepository.save(like);

        // when
        Optional<Like> foundLike = likeRepository.findByMemberIdAndDiaryId(member.getId(), diary.getId());

        // then
        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getMember()).isEqualTo(member);
        assertThat(foundLike.get().getDiary()).isEqualTo(diary);
    }

    @Test
    @DisplayName("좋아요 여부가 성공적으로 확인된다.")
    void should_CheckExistence_When_CheckingLike() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Like like = Like.builder()
                .member(member)
                .diary(diary)
                .build();
        likeRepository.save(like);

        // when & then
        assertThat(likeRepository.existsByMemberIdAndDiaryId(member.getId(), diary.getId()))
                .isTrue();
        assertThat(likeRepository.existsByMemberIdAndDiaryId(member.getId(), 999L))
                .isFalse();
    }

    @Test
    @DisplayName("좋아요가 성공적으로 삭제된다.")
    void should_DeleteLike_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Like like = Like.builder()
                .member(member)
                .diary(diary)
                .build();
        Like savedLike = likeRepository.save(like);

        // when
        likeRepository.delete(savedLike);

        // then
        Optional<Like> deletedLike = likeRepository.findById(savedLike.getId());
        assertThat(deletedLike).isEmpty();
    }

    private Member createAndSaveMember() {
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        return memberRepository.save(member);
    }

    private Diary createAndSaveDiary(Member member) {
        Point point = new GeometryFactory().createPoint(new Coordinate(37.5665, 126.978));

        Diary diary = Diary.builder()
                .member(member)
                .content("Test content")
                .locationName("Test location")
                .address("Test address")
                .locationPoint(point)
                .build();
        return diaryRepository.save(diary);
    }

}
