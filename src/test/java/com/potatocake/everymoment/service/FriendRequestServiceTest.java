package com.potatocake.everymoment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.response.FriendRequestPageRequest;
import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.FriendRequest;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.FriendRequestRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.util.PagingUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceTest {

    @InjectMocks
    private FriendRequestService friendRequestService;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PagingUtil pagingUtil;

    @Test
    @DisplayName("친구 요청 목록이 성공적으로 조회된다.")
    void should_GetFriendRequests_When_ValidRequest() {
        // given
        Long memberId = 1L;
        Long key = null;
        int size = 10;

        Member requester = Member.builder()
                .id(2L)
                .nickname("requester")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(1L)
                .sender(requester)
                .build();

        ScrollPosition scrollPosition = ScrollPosition.offset();
        Pageable pageable = PageRequest.of(0, size);
        Window<FriendRequest> window = Window.from(List.of(request), i -> scrollPosition, false);

        given(pagingUtil.createScrollPosition(key)).willReturn(scrollPosition);
        given(pagingUtil.createPageable(size, Sort.Direction.DESC)).willReturn(pageable);
        given(friendRequestRepository.findByReceiverId(memberId, scrollPosition, pageable))
                .willReturn(window);
        given(memberRepository.findAllById(any())).willReturn(List.of(requester));

        given(pagingUtil.getNextKey(any(), any())).willReturn(null);

        // when
        FriendRequestPageRequest response = friendRequestService.getFriendRequests(key, size, memberId);

        // then
        assertThat(response.getFriendRequests()).hasSize(1);
        assertThat(response.getFriendRequests().get(0).getNickname()).isEqualTo("requester");
        assertThat(response.getNext()).isNull();
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 전송된다.")
    void should_SendFriendRequest_When_ValidRequest() {
        // given
        Long senderId = 1L;
        Long receiverId = 2L;
        Member sender = Member.builder()
                .id(senderId)
                .nickname("sender")
                .build();
        Member receiver = Member.builder()
                .id(receiverId)
                .build();

        FriendRequest savedRequest = FriendRequest.builder()
                .id(1L)  // ID 설정
                .sender(sender)
                .receiver(receiver)
                .build();

        given(memberRepository.findById(senderId)).willReturn(Optional.of(sender));
        given(memberRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(friendRepository.existsByMemberIdAndFriendId(senderId, receiverId)).willReturn(false);
        given(friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId))
                .willReturn(false);
        given(friendRequestRepository.save(any(FriendRequest.class)))
                .willReturn(savedRequest);

        // when
        friendRequestService.sendFriendRequest(senderId, receiverId);

        // then
        then(friendRequestRepository).should().save(any(FriendRequest.class));
        then(notificationService).should().createAndSendNotification(
                eq(receiverId),
                eq(NotificationType.FRIEND_REQUEST),
                eq(savedRequest.getId()),
                eq(sender.getNickname())
        );
    }

    @Test
    @DisplayName("이미 친구인 사용자에게 요청을 보내면 예외가 발생한다.")
    void should_ThrowException_When_AlreadyFriends() {
        // given
        Long senderId = 1L;
        Long receiverId = 2L;

        given(friendRepository.existsByMemberIdAndFriendId(senderId, receiverId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> friendRequestService.sendFriendRequest(senderId, receiverId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_FRIEND);
    }

    @Test
    @DisplayName("이미 요청을 보낸 사용자에게 다시 요청을 보내면 예외가 발생한다.")
    void should_ThrowException_When_RequestAlreadyExists() {
        // given
        Long senderId = 1L;
        Long receiverId = 2L;

        given(friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> friendRequestService.sendFriendRequest(senderId, receiverId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 수락된다.")
    void should_AcceptFriendRequest_When_ValidRequest() {
        // given
        Long requestId = 1L;
        Long receiverId = 1L;

        Member sender = Member.builder()
                .id(2L)
                .nickname("sender")
                .build();
        Member receiver = Member.builder()
                .id(receiverId)
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .sender(sender)
                .receiver(receiver)
                .build();

        given(friendRequestRepository.findById(requestId))
                .willReturn(Optional.of(request));

        // when
        friendRequestService.acceptFriendRequest(requestId, receiverId);

        // then
        then(friendRepository).should(times(2)).save(any(Friend.class));
        then(friendRequestRepository).should().delete(request);
        then(notificationService).should().createAndSendNotification(
                eq(sender.getId()),
                eq(NotificationType.FRIEND_ACCEPT),
                eq(receiver.getId()),
                eq(receiver.getNickname())
        );
    }

    @Test
    @DisplayName("다른 사용자의 친구 요청을 수락하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_AcceptingOtherUserRequest() {
        // given
        Long requestId = 1L;
        Long receiverId = 1L;
        Long otherUserId = 3L;

        Member sender = Member.builder()
                .id(2L)
                .build();
        Member receiver = Member.builder()
                .id(otherUserId)  // 다른 사용자
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .sender(sender)
                .receiver(receiver)
                .build();

        given(friendRequestRepository.findById(requestId))
                .willReturn(Optional.of(request));

        // when & then
        assertThatThrownBy(() -> friendRequestService.acceptFriendRequest(requestId, receiverId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FRIEND_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("친구 요청이 성공적으로 거절된다.")
    void should_RejectFriendRequest_When_ValidRequest() {
        // given
        Long requestId = 1L;
        Long receiverId = 1L;

        Member sender = Member.builder()
                .id(2L)
                .build();
        Member receiver = Member.builder()
                .id(receiverId)
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .sender(sender)
                .receiver(receiver)
                .build();

        given(friendRequestRepository.findById(requestId))
                .willReturn(Optional.of(request));

        // when
        friendRequestService.rejectFriendRequest(requestId, receiverId);

        // then
        then(friendRequestRepository).should().delete(request);
    }

    @Test
    @DisplayName("다른 사용자의 친구 요청을 거절하려고 하면 예외가 발생한다.")
    void should_ThrowException_When_RejectingOtherUserRequest() {
        // given
        Long requestId = 1L;
        Long receiverId = 1L;
        Long otherUserId = 3L;

        Member sender = Member.builder()
                .id(2L)
                .build();
        Member receiver = Member.builder()
                .id(otherUserId)  // 다른 사용자
                .build();

        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .sender(sender)
                .receiver(receiver)
                .build();

        given(friendRequestRepository.findById(requestId))
                .willReturn(Optional.of(request));

        // when & then
        assertThatThrownBy(() -> friendRequestService.rejectFriendRequest(requestId, receiverId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FRIEND_REQUEST_NOT_FOUND);
    }

}
