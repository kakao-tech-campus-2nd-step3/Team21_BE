package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.LocationPoint;
import com.potatocake.everymoment.dto.request.CategoryRequest;
import com.potatocake.everymoment.dto.request.DiaryAutoCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryFilterRequest;
import com.potatocake.everymoment.dto.request.DiaryManualCreateRequest;
import com.potatocake.everymoment.dto.request.DiaryPatchRequest;
import com.potatocake.everymoment.dto.response.MyDiariesResponse;
import com.potatocake.everymoment.dto.response.MyDiaryResponse;
import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.CategoryRepository;
import com.potatocake.everymoment.repository.DiaryCategoryRepository;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.repository.LikeRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DiaryCategoryRepository diaryCategoryRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private GeometryFactory geometryFactory;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("자동 일기가 성공적으로 저장된다.")
    void should_SaveAutoDiary_When_ValidInput() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        DiaryAutoCreateRequest request = DiaryAutoCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.978))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .build();

        Point point = mock(Point.class);
        Diary savedDiary = Diary.builder()
                .id(1L)
                .member(member)
                .locationPoint(point)
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(geometryFactory.createPoint(any(Coordinate.class))).willReturn(point);
        given(diaryRepository.save(any(Diary.class))).willReturn(savedDiary);

        // when
        diaryService.createDiaryAuto(memberId, request);

        // then
        then(memberRepository).should().findById(memberId);
        then(geometryFactory).should().createPoint(any(Coordinate.class));
        then(diaryRepository).should().save(any(Diary.class));
        then(notificationService).should().createAndSendNotification(
                eq(memberId),
                eq(NotificationType.MOOD_CHECK),
                eq(savedDiary.getId()),
                eq(savedDiary.getLocationName())
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원이 자동 일기를 작성하려 하면 예외가 발생한다.")
    void should_ThrowException_When_MemberNotFoundInAutoCreate() {
        // given
        Long memberId = 1L;
        DiaryAutoCreateRequest request = DiaryAutoCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.978))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiaryAuto(memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("수동 일기가 성공적으로 저장된다.")
    void should_SaveManualDiary_When_ValidInput() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Long categoryId = 1L;
        Category category = Category.builder()
                .id(categoryId)
                .member(member)
                .build();

        DiaryManualCreateRequest request = DiaryManualCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.978))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .content("Test content")
                .emoji("😊")
                .categories(List.of(new CategoryRequest(categoryId)))
                .isBookmark(false)
                .isPublic(true)
                .build();

        Point point = mock(Point.class);
        Diary savedDiary = Diary.builder()
                .id(1L)
                .member(member)
                .locationPoint(point)
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .content("Test content")
                .emoji("😊")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(geometryFactory.createPoint(any(Coordinate.class))).willReturn(point);
        given(diaryRepository.save(any(Diary.class))).willReturn(savedDiary);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        diaryService.createDiaryManual(memberId, request);

        // then
        then(memberRepository).should().findById(memberId);
        then(geometryFactory).should().createPoint(any(Coordinate.class));
        then(diaryRepository).should().save(any(Diary.class));
        then(categoryRepository).should().findById(categoryId);
        then(diaryCategoryRepository).should().save(any(DiaryCategory.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이 수동 일기를 작성하려 하면 예외가 발생한다.")
    void should_ThrowException_When_MemberNotFoundInManualCreate() {
        // given
        Long memberId = 1L;
        DiaryManualCreateRequest request = DiaryManualCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.978))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiaryManual(memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 수동 일기를 작성하려 하면 예외가 발생한다.")
    void should_ThrowException_When_CategoryNotFoundInManualCreate() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Long categoryId = 1L;
        DiaryManualCreateRequest request = DiaryManualCreateRequest.builder()
                .locationPoint(new LocationPoint(37.5665, 126.978))
                .locationName("Seoul")
                .address("Seoul, South Korea")
                .categories(List.of(new CategoryRequest(categoryId)))
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.createDiaryManual(memberId, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("내 일기 목록이 성공적으로 조회된다.")
    void should_ReturnMyDiaries_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        DiaryFilterRequest filterRequest = DiaryFilterRequest.builder()
                .key(0)
                .size(10)
                .build();

        Diary diary = Diary.builder()
                .id(1L)
                .member(member)
                .content("Test content")
                .locationName("Seoul")
                .build();

        Page<Diary> diaryPage = new PageImpl<>(List.of(diary));

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(diaryPage);

        // when
        MyDiariesResponse response = diaryService.getMyDiaries(memberId, filterRequest);

        // then
        assertThat(response.getDiaries()).hasSize(1);
        assertThat(response.getNext()).isNull();
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("내 일기가 성공적으로 조회된다.")
    void should_ReturnMyDiary_When_ValidId() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .content("Test content")
                .locationName("Seoul")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(diaryCategoryRepository.findByDiary(diary)).willReturn(List.of());
        given(likeRepository.existsByMemberIdAndDiaryId(memberId, diaryId)).willReturn(false);

        // when
        MyDiaryResponse response = diaryService.getMyDiary(memberId, diaryId);

        // then
        assertThat(response.getId()).isEqualTo(diaryId);
        assertThat(response.getContent()).isEqualTo("Test content");
        assertThat(response.getLocationName()).isEqualTo("Seoul");
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
        then(diaryCategoryRepository).should().findByDiary(diary);
        then(likeRepository).should().existsByMemberIdAndDiaryId(memberId, diaryId);
    }

    @Test
    @DisplayName("다른 사용자의 일기를 조회하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_NotMyDiary() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Member otherMember = Member.builder()
                .id(2L)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(otherMember)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when & then
        assertThatThrownBy(() -> diaryService.getMyDiary(memberId, diaryId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIARY_NOT_FOUND);
    }

    @Test
    @DisplayName("일기의 위치 정보가 성공적으로 조회된다.")
    void should_ReturnLocation_When_ValidId() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();

        Point point = mock(Point.class);
        given(point.getX()).willReturn(37.5665);
        given(point.getY()).willReturn(126.978);

        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .locationPoint(point)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        LocationPoint location = diaryService.getDiaryLocation(memberId, diaryId);

        // then
        assertThat(location.getLatitude()).isEqualTo(37.5665);
        assertThat(location.getLongitude()).isEqualTo(126.978);
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
    }

    @Test
    @DisplayName("일기가 성공적으로 수정된다.")
    void should_UpdateDiary_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Long categoryId = 1L;

        Member member = Member.builder()
                .id(memberId)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .member(member)
                .build();

        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .content("Original content")
                .build();

        DiaryPatchRequest request = DiaryPatchRequest.builder()
                .content("Updated content")
                .locationName("Updated location")
                .categories(List.of(new CategoryRequest(categoryId)))
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        diaryService.updateDiary(memberId, diaryId, request);

        // then
        assertThat(diary.getContent()).isEqualTo("Updated content");
        assertThat(diary.getLocationName()).isEqualTo("Updated location");
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
        then(diaryCategoryRepository).should().deleteByDiary(diary);
        then(categoryRepository).should().findById(categoryId);
        then(diaryCategoryRepository).should().save(any(DiaryCategory.class));
    }

    @Test
    @DisplayName("일기가 성공적으로 삭제된다.")
    void should_DeleteDiary_When_ValidId() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        diaryService.deleteDiary(memberId, diaryId);

        // then
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
        then(diaryRepository).should().delete(diary);
    }

    @Test
    @DisplayName("북마크가 성공적으로 토글된다.")
    void should_ToggleBookmark_When_ValidId() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .isBookmark(false)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        diaryService.toggleBookmark(memberId, diaryId);

        // then
        assertThat(diary.isBookmark()).isTrue();
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
    }

    @Test
    @DisplayName("공개 상태가 성공적으로 토글된다.")
    void should_TogglePrivacy_When_ValidId() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .isPublic(false)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        diaryService.togglePrivacy(memberId, diaryId);

        // then
        assertThat(diary.isPublic()).isTrue();
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
    }

    @Test
    @DisplayName("일기 내용을 삭제하면 null로 설정된다.")
    void should_SetContentNull_When_ContentDeleteIsTrue() {
        // given
        Long memberId = 1L;
        Long diaryId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .build();
        Diary diary = Diary.builder()
                .id(diaryId)
                .member(member)
                .content("Original content")
                .build();

        DiaryPatchRequest request = DiaryPatchRequest.builder()
                .contentDelete(true)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

        // when
        diaryService.updateDiary(memberId, diaryId, request);

        // then
        assertThat(diary.getContent()).isNull();
        then(memberRepository).should().findById(memberId);
        then(diaryRepository).should().findById(diaryId);
    }

}
