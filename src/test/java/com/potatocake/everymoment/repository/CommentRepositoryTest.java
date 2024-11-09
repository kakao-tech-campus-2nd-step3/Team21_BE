package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Comment;
import com.potatocake.everymoment.entity.Diary;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Test
    @DisplayName("댓글이 성공적으로 저장된다.")
    void should_SaveComment_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content("Test comment")
                .build();

        // when
        Comment savedComment = commentRepository.save(comment);

        // then
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("Test comment");
        assertThat(savedComment.getMember()).isEqualTo(member);
        assertThat(savedComment.getDiary()).isEqualTo(diary);
    }

    @Test
    @DisplayName("일기의 댓글 목록이 성공적으로 조회된다.")
    void should_FindComments_When_FilteringByDiaryId() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        Comment comment1 = Comment.builder()
                .member(member)
                .diary(diary)
                .content("Comment 1")
                .build();

        Comment comment2 = Comment.builder()
                .member(member)
                .diary(diary)
                .content("Comment 2")
                .build();

        commentRepository.saveAll(List.of(comment1, comment2));

        // when
        Page<Comment> comments = commentRepository.findAllByDiaryId(
                diary.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(comments.getContent()).hasSize(2);
        assertThat(comments.getContent())
                .extracting("content")
                .containsExactly("Comment 1", "Comment 2");
    }

    @Test
    @DisplayName("페이징이 성공적으로 동작한다.")
    void should_ReturnPagedResult_When_UsingPagination() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        List<Comment> comments = List.of(
                Comment.builder()
                        .member(member)
                        .diary(diary)
                        .content("Comment 1")
                        .build(),
                Comment.builder()
                        .member(member)
                        .diary(diary)
                        .content("Comment 2")
                        .build(),
                Comment.builder()
                        .member(member)
                        .diary(diary)
                        .content("Comment 3")
                        .build()
        );

        commentRepository.saveAll(comments);

        // when
        Page<Comment> firstPage = commentRepository.findAllByDiaryId(
                diary.getId(),
                PageRequest.of(0, 2)
        );

        Page<Comment> secondPage = commentRepository.findAllByDiaryId(
                diary.getId(),
                PageRequest.of(1, 2)
        );

        // then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent())
                .extracting("content")
                .containsExactly("Comment 1", "Comment 2");

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.getContent())
                .extracting("content")
                .containsExactly("Comment 3");
    }

    @Test
    @DisplayName("다른 일기의 댓글은 조회되지 않는다.")
    void should_NotFindComments_When_DifferentDiary() {
        // given
        Member member = createAndSaveMember();
        Diary diary1 = createAndSaveDiary(member);
        Diary diary2 = createAndSaveDiary(member);

        Comment comment1 = Comment.builder()
                .member(member)
                .diary(diary1)
                .content("Comment for diary 1")
                .build();

        Comment comment2 = Comment.builder()
                .member(member)
                .diary(diary2)
                .content("Comment for diary 2")
                .build();

        commentRepository.saveAll(List.of(comment1, comment2));

        // when
        Page<Comment> comments = commentRepository.findAllByDiaryId(
                diary1.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(comments.getContent()).hasSize(1);
        assertThat(comments.getContent())
                .extracting("content")
                .containsExactly("Comment for diary 1");
    }

    @Test
    @DisplayName("빈 페이지가 성공적으로 반환된다.")
    void should_ReturnEmptyPage_When_NoComments() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        // when
        Page<Comment> comments = commentRepository.findAllByDiaryId(
                diary.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(comments.getContent()).isEmpty();
        assertThat(comments.getTotalElements()).isZero();
        assertThat(comments.hasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글이 성공적으로 삭제된다.")
    void should_DeleteComment_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);
        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content("Test comment")
                .build();
        Comment savedComment = commentRepository.save(comment);

        // when
        commentRepository.delete(savedComment);

        // then
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());
        assertThat(foundComment).isEmpty();
    }

    private Member createAndSaveMember() {
        Member member = Member.builder()
                .number(1234L)
                .nickname("testUser")
                .profileImageUrl("https://example.com/image.jpg")
                .build();
        return memberRepository.save(member);
    }

    private Diary createAndSaveDiary(Member member) {
        Point point = new GeometryFactory().createPoint(new Coordinate(37.5665, 126.978));

        Diary diary = Diary.builder()
                .member(member)
                .content("Test diary")
                .locationName("Test location")
                .address("Test address")
                .locationPoint(point)
                .build();
        return diaryRepository.save(diary);
    }

}
