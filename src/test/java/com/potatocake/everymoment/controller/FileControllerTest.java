package com.potatocake.everymoment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.FileService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    @DisplayName("파일 목록이 성공적으로 조회된다.")
    void should_ReturnFiles_When_ValidDiaryId() throws Exception {
        // given
        Long diaryId = 1L;
        List<FileResponse> responses = List.of(
                FileResponse.builder()
                        .id(1L)
                        .imageUrl("https://example.com/image1.jpg")
                        .order(1)
                        .build(),
                FileResponse.builder()
                        .id(2L)
                        .imageUrl("https://example.com/image2.jpg")
                        .order(2)
                        .build()
        );

        given(fileService.getFiles(diaryId)).willReturn(responses);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/{diaryId}/files", diaryId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info").isArray())
                .andExpect(jsonPath("$.info[0].imageUrl").value("https://example.com/image1.jpg"))
                .andExpect(jsonPath("$.info[1].imageUrl").value("https://example.com/image2.jpg"));

        then(fileService).should().getFiles(diaryId);
    }

    @Test
    @DisplayName("파일이 성공적으로 업로드된다.")
    void should_UploadFiles_When_ValidInput() throws Exception {
        // given
        Long diaryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image 2".getBytes()
        );

        willDoNothing().given(fileService).uploadFiles(diaryId, memberId, List.of(file1, file2));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/diaries/{diaryId}/files", diaryId)
                .file(file1)
                .file(file2)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(fileService).should().uploadFiles(diaryId, memberId, List.of(file1, file2));
    }

    @Test
    @DisplayName("파일이 성공적으로 수정된다.")
    void should_UpdateFiles_When_ValidInput() throws Exception {
        // given
        Long diaryId = 1L;
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        willDoNothing().given(fileService).updateFiles(diaryId, memberId, List.of(file));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/diaries/{diaryId}/files", diaryId)
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(fileService).should().updateFiles(diaryId, memberId, List.of(file));
    }

}
