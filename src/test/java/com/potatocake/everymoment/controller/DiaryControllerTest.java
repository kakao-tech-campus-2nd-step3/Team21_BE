package com.potatocake.everymoment.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.dto.LocationPoint;
import com.potatocake.everymoment.dto.request.CategoryRequest;
import com.potatocake.everymoment.dto.request.CommentRequest;
import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryPatchRequest;
import com.potatocake.everymoment.dto.response.CommentResponse;
import com.potatocake.everymoment.dto.response.CommentsResponse;
import com.potatocake.everymoment.dto.response.FriendDiariesResponse;
import com.potatocake.everymoment.dto.response.FriendDiaryResponse;
import com.potatocake.everymoment.dto.response.FriendDiarySimpleResponse;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.dto.response.MyDiarySimpleResponse;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.security.MemberDetails;
import com.potatocake.everymoment.service.CommentService;
import com.potatocake.everymoment.service.DiaryService;
import com.potatocake.everymoment.service.FriendDiaryService;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
@WebMvcTest(DiaryController.class)
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiaryService diaryService;

    @MockBean
    private FriendDiaryService friendDiaryService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("유효한 입력으로 자동 일기가 성공적으로 작성된다.")
    void should_CreateAutoEntry_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        DiaryAutoCreateRequest request = DiaryAutoCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.9780))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .build();

        willDoNothing().given(diaryService).createDiaryAuto(memberId, request);

        // when
        ResultActions result = mockMvc.perform(post("/api/diaries/auto")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().createDiaryAuto(
                eq(memberId),
                argThat(req ->
                        Objects.equals(req.getLocationName(), "Seoul") &&
                                Objects.equals(req.getAddress(), "Seoul, South Korea") &&
                                Objects.equals(req.getLocationPoint().getLatitude(), 37.5665) &&
                                Objects.equals(req.getLocationPoint().getLongitude(), 126.978)
                )
        );
    }

    @Test
    @DisplayName("유효한 입력으로 수동 일기가 성공적으로 작성된다.")
    void should_CreateManualEntry_When_ValidInput() throws Exception {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        LocationPoint locationPoint = new LocationPoint(37.5665, 126.978);

        DiaryManualCreateRequest request = DiaryManualCreateRequest.builder()
                .locationPoint(locationPoint)
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .content("Test content")
                .emoji("😊")
                .categories(List.of(new CategoryRequest(1L)))
                .isBookmark(false)
                .isPublic(true)
                .build();

        willDoNothing().given(diaryService).createDiaryManual(memberId, request);

        // when
        ResultActions result = mockMvc.perform(post("/api/diaries/manual")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().createDiaryManual(
                eq(memberId),
                argThat(req ->
                        Objects.equals(req.getContent(), "Test content") &&
                                Objects.equals(req.getLocationName(), "Seoul") &&
                                Objects.equals(req.getAddress(), "Seoul, South Korea") &&
                                Objects.equals(req.getLocationPoint().getLatitude(), 37.5665) &&
                                Objects.equals(req.getLocationPoint().getLongitude(), 126.978) &&
                                Objects.equals(req.getEmoji(), "😊") &&
                                Objects.equals(req.isPublic(), true) &&
                                Objects.equals(req.isBookmark(), false) &&
                                req.getCategories().size() == 1 &&
                                Objects.equals(req.getCategories().get(0).getCategoryId(), 1L)
                )
        );
    }

    @Test
    @DisplayName("내 일기 목록이 성공적으로 조회된다.")
    void should_ReturnMyDiaries_When_ValidRequest() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MyDiariesResponse response = MyDiariesResponse.builder()
                .diaries(List.of(MyDiarySimpleResponse.builder()
                        .id(1L)
                        .content("Test content")
                        .locationName("Seoul")
                        .build()))
                .next(null)
                .build();

        given(diaryService.getMyDiaries(eq(member.getId()), any(DiaryFilterRequest.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/my")
                .with(user(memberDetails))
                .param("key", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.diaries").isArray());

        then(diaryService).should().getMyDiaries(eq(member.getId()), any(DiaryFilterRequest.class));
    }

    @Test
    @DisplayName("내 일기 상세 정보가 성공적으로 조회된다.")
    void should_ReturnMyDiaryDetail_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        MyDiaryResponse response = MyDiaryResponse.builder()
                .id(diaryId)
                .content("Test content")
                .locationName("Seoul")
                .build();

        given(diaryService.getMyDiary(member.getId(), diaryId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/my/{diaryId}", diaryId)
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.id").value(diaryId));

        then(diaryService).should().getMyDiary(member.getId(), diaryId);
    }

    @Test
    @DisplayName("일기의 위치 정보가 성공적으로 조회된다.")
    void should_ReturnDiaryLocation_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        LocationPoint locationPoint = new LocationPoint(37.5665, 126.9780);
        given(diaryService.getDiaryLocation(member.getId(), diaryId)).willReturn(locationPoint);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/{diaryId}/location", diaryId)
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.latitude").value(37.5665))
                .andExpect(jsonPath("$.info.longitude").value(126.9780));

        then(diaryService).should().getDiaryLocation(member.getId(), diaryId);
    }

    @Test
    @DisplayName("일기가 성공적으로 수정된다.")
    void should_UpdateDiary_When_ValidInput() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        DiaryPatchRequest request = DiaryPatchRequest.builder()
                .content("Updated content")
                .locationName("Updated location")
                .address("Updated address")
                .emoji("😊")
                .categories(List.of(new CategoryRequest(1L)))
                .build();

        willDoNothing().given(diaryService).updateDiary(member.getId(), diaryId, request);

        // when
        ResultActions result = mockMvc.perform(patch("/api/diaries/{diaryId}", diaryId)
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().updateDiary(
                eq(member.getId()),
                eq(diaryId),
                argThat(req ->
                        Objects.equals(req.getContent(), "Updated content") &&
                                Objects.equals(req.getLocationName(), "Updated location") &&
                                Objects.equals(req.getAddress(), "Updated address") &&
                                Objects.equals(req.getEmoji(), "😊") &&
                                req.getCategories().size() == 1 &&
                                Objects.equals(req.getCategories().get(0).getCategoryId(), 1L))
        );
    }

    @Test
    @DisplayName("일기가 성공적으로 삭제된다.")
    void should_DeleteDiary_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(diaryService).deleteDiary(member.getId(), diaryId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/diaries/{diaryId}", diaryId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().deleteDiary(member.getId(), diaryId);
    }

    @Test
    @DisplayName("일기의 북마크 상태가 성공적으로 토글된다.")
    void should_ToggleBookmark_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(diaryService).toggleBookmark(member.getId(), diaryId);

        // when
        ResultActions result = mockMvc.perform(patch("/api/diaries/{diaryId}/bookmark", diaryId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().toggleBookmark(member.getId(), diaryId);
    }

    @Test
    @DisplayName("일기의 공개 상태가 성공적으로 토글된다.")
    void should_TogglePrivacy_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        willDoNothing().given(diaryService).togglePrivacy(member.getId(), diaryId);

        // when
        ResultActions result = mockMvc.perform(patch("/api/diaries/{diaryId}/privacy", diaryId)
                .with(user(memberDetails))
                .with(csrf()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(diaryService).should().togglePrivacy(member.getId(), diaryId);
    }

    @Test
    @DisplayName("친구의 일기 목록이 성공적으로 조회된다.")
    void should_ReturnFriendDiaries_When_ValidRequest() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FriendDiariesResponse response = FriendDiariesResponse.builder()
                .diaries(List.of(FriendDiarySimpleResponse.builder()
                        .id(1L)
                        .content("Friend's content")
                        .locationName("Friend's location")
                        .build()))
                .next(null)
                .build();

        given(friendDiaryService.getFriendDiaries(eq(member.getId()), any(DiaryFilterRequest.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/friend")
                .with(user(memberDetails))
                .param("key", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.diaries").isArray());

        then(friendDiaryService).should().getFriendDiaries(eq(member.getId()), any(DiaryFilterRequest.class));
    }

    @Test
    @DisplayName("친구의 일기 상세 정보가 성공적으로 조회된다.")
    void should_ReturnFriendDiaryDetail_When_ValidId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        FriendDiaryResponse response = FriendDiaryResponse.builder()
                .id(diaryId)
                .content("Friend's content")
                .locationName("Friend's location")
                .build();

        given(friendDiaryService.getFriendDiary(member.getId(), diaryId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/friend/{diaryId}", diaryId)
                .with(user(memberDetails)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.id").value(diaryId));

        then(friendDiaryService).should().getFriendDiary(member.getId(), diaryId);
    }

    @Test
    @DisplayName("일기의 댓글 목록이 성공적으로 조회된다.")
    void should_ReturnComments_When_ValidDiaryId() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CommentsResponse response = CommentsResponse.builder()
                .comments(List.of(CommentResponse.builder()
                        .id(1L)
                        .content("Test comment")
                        .build()))
                .next(null)
                .build();

        given(commentService.getComments(diaryId, 0, 10)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/diaries/{diaryId}/comments", diaryId)
                .with(user(memberDetails))
                .param("key", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.info.comments").isArray());

        then(commentService).should().getComments(diaryId, 0, 10);
    }

    @Test
    @DisplayName("댓글이 성공적으로 작성된다.")
    void should_CreateComment_When_ValidInput() throws Exception {
        // given
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        CommentRequest request = new CommentRequest();
        request.setContent("Test comment");

        willDoNothing().given(commentService).createComment(member.getId(), diaryId, request);

        // when
        ResultActions result = mockMvc.perform(post("/api/diaries/{diaryId}/comments", diaryId)
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        then(commentService).should().createComment(
                eq(member.getId()),
                eq(diaryId),
                argThat(req -> req.getContent().equals("Test comment"))
        );
    }

    @Test
    @DisplayName("검증 실패시 수동 일기 작성에 실패한다.")
    void should_FailToCreateManualEntry_When_ValidationFails() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .number(1234L)
                .nickname("testUser")
                .build();
        MemberDetails memberDetails = new MemberDetails(member);

        DiaryManualCreateRequest request = DiaryManualCreateRequest.builder()
                .content("x".repeat(15001)) // 최대 길이 초과
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/diaries/manual")
                .with(user(memberDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        then(diaryService).shouldHaveNoInteractions();
    }

}
