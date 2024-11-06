package com.potatocake.everymoment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
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
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("파일이 성공적으로 저장된다.")
    void should_SaveFile_When_ValidEntity() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        File file = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image.jpg")
                .order(1)
                .build();

        // when
        File savedFile = fileRepository.save(file);

        // then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(savedFile.getOrder()).isEqualTo(1);
        assertThat(savedFile.getDiary()).isEqualTo(diary);
    }

    @Test
    @DisplayName("일기의 파일 목록이 성공적으로 조회된다.")
    void should_FindFiles_When_FilteringByDiaryId() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        File file1 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image1.jpg")
                .order(1)
                .build();

        File file2 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image2.jpg")
                .order(2)
                .build();

        fileRepository.saveAll(List.of(file1, file2));

        // when
        List<File> files = fileRepository.findByDiaryId(diary.getId());

        // then
        assertThat(files).hasSize(2);
        assertThat(files).extracting("imageUrl")
                .containsExactly(
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg"
                );
    }

    @Test
    @DisplayName("다이어리별로 파일이 독립적으로 조회된다.")
    void should_FindFilesSeparately_WhenMultipleDiaries() {
        // given
        Member member = createAndSaveMember();
        Diary diary1 = createAndSaveDiary(member);
        Diary diary2 = createAndSaveDiary(member);

        File file1 = File.builder()
                .diary(diary1)
                .imageUrl("https://example.com/diary1/image.jpg")
                .order(1)
                .build();

        File file2 = File.builder()
                .diary(diary2)
                .imageUrl("https://example.com/diary2/image.jpg")
                .order(1)
                .build();

        fileRepository.saveAll(List.of(file1, file2));

        // when
        List<File> filesForDiary1 = fileRepository.findByDiaryId(diary1.getId());
        List<File> filesForDiary2 = fileRepository.findByDiaryId(diary2.getId());

        // then
        assertThat(filesForDiary1)
                .hasSize(1)
                .extracting("imageUrl")
                .containsExactly("https://example.com/diary1/image.jpg");

        assertThat(filesForDiary2)
                .hasSize(1)
                .extracting("imageUrl")
                .containsExactly("https://example.com/diary2/image.jpg");
    }

    @Test
    @DisplayName("특정 순서의 파일이 성공적으로 조회된다.")
    void should_FindFile_When_FilteringByDiaryAndOrder() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        File file1 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image1.jpg")
                .order(1)
                .build();

        File file2 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image2.jpg")
                .order(2)
                .build();

        fileRepository.saveAll(List.of(file1, file2));

        // when
        File foundFile = fileRepository.findByDiaryAndOrder(diary, 2);

        // then
        assertThat(foundFile).isNotNull();
        assertThat(foundFile.getImageUrl()).isEqualTo("https://example.com/image2.jpg");
        assertThat(foundFile.getOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("일기의 모든 파일이 성공적으로 삭제된다.")
    void should_DeleteAllFiles_When_DeletingByDiary() {
        // given
        Member member = createAndSaveMember();
        Diary diary = createAndSaveDiary(member);

        File file1 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image1.jpg")
                .order(1)
                .build();

        File file2 = File.builder()
                .diary(diary)
                .imageUrl("https://example.com/image2.jpg")
                .order(2)
                .build();

        fileRepository.saveAll(List.of(file1, file2));

        // when
        fileRepository.deleteByDiary(diary);

        // then
        List<File> remainingFiles = fileRepository.findByDiaryId(diary.getId());
        assertThat(remainingFiles).isEmpty();
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
                .content("Test diary")
                .locationName("Test location")
                .address("Test address")
                .locationPoint(point)
                .build();
        return diaryRepository.save(diary);
    }

}
