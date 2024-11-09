package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.util.S3FileUploader;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private S3FileUploader uploader;

    @Test
    @DisplayName("파일 목록이 성공적으로 조회된다.")
    void should_GetFiles_When_ValidDiaryId() {
        // given
        Long diaryId = 1L;
        List<File> files = List.of(
                File.builder()
                        .id(1L)
                        .imageUrl("https://example.com/image1.jpg")
                        .order(1)
                        .build(),
                File.builder()
                        .id(2L)
                        .imageUrl("https://example.com/image2.jpg")
                        .order(2)
                        .build()
        );

        given(fileRepository.findByDiaryId(diaryId)).willReturn(files);

        // when
        List<FileResponse> responses = fileService.getFiles(diaryId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("imageUrl")
                .containsExactly(
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg"
                );
    }

    @Test
    @DisplayName("파일이 성공적으로 업로드된다.")
    void should_UploadFiles_When_ValidInput() {
        // given
        Long diaryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .build();

        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1, file2);

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(uploader.uploadFile(any(MultipartFile.class)))
                .willReturn("https://example.com/image1.jpg")
                .willReturn("https://example.com/image2.jpg");

        // when
        fileService.uploadFiles(diaryId, memberId, files);

        // then
        then(fileRepository).should().saveAll(anyList());
        then(uploader).should().uploadFile(file1);
        then(uploader).should().uploadFile(file2);
    }

    @Test
    @DisplayName("파일이 성공적으로 수정된다.")
    void should_UpdateFiles_When_ValidInput() {
        // given
        Long diaryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(uploader.uploadFile(file)).willReturn("https://example.com/new-image.jpg");

        // when
        fileService.updateFiles(diaryId, memberId, files);

        // then
        then(fileRepository).should().deleteByDiary(diary);
        then(fileRepository).should().saveAll(anyList());
    }

}
