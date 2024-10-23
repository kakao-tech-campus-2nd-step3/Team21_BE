package com.potatocake.everymoment.service;

import static java.util.function.Function.identity;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.potatocake.everymoment.constant.NotificationType;
import com.potatocake.everymoment.dto.response.FriendRequestPageRequest;
import com.potatocake.everymoment.dto.response.FriendRequestResponse;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final PagingUtil pagingUtil;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public FriendRequestPageRequest getFriendRequests(Long key, int size, Long memberId) {
        Window<FriendRequest> window = fetchFriendRequestWindow(key, size, memberId);
        List<FriendRequestResponse> requests = convertToFriendRequestResponses(window.getContent());
        Long nextKey = pagingUtil.getNextKey(window, FriendRequest::getId);

        return FriendRequestPageRequest.builder()
                .friendRequests(requests)
                .next(nextKey)
                .build();
    }

    public void sendFriendRequest(Long senderId, Long receiverId) {
        // 이미 친구인 경우 체크
        if (friendRepository.existsByMemberIdAndFriendId(senderId, receiverId)) {
            throw new GlobalException(ErrorCode.ALREADY_FRIEND);
        }

        // 이미 친구 요청을 보낸 경우 체크
        if (friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
            throw new GlobalException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        // 상대방이 나에게 보낸 친구 요청이 있는지 확인
        Optional<FriendRequest> oppositeRequest = friendRequestRepository
                .findBySenderIdAndReceiverId(receiverId, senderId);

        if (oppositeRequest.isPresent()) {
            // 상대방이 이미 나에게 친구 요청을 보낸 상태라면 자동으로 수락 처리
            createFriendRelationship(receiver, sender);
            friendRequestRepository.delete(oppositeRequest.get());

            // 상대방에게 친구 수락 알림 발송
            notificationService.createAndSendNotification(
                    receiverId,
                    NotificationType.FRIEND_ACCEPT,
                    sender.getId(),
                    sender.getNickname()
            );
        } else {
            // 상대방이 보낸 요청이 없다면 새로운 친구 요청 생성
            FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .build());

            // 상대방에게 친구 요청 알림 발송
            notificationService.createAndSendNotification(
                    receiverId,
                    NotificationType.FRIEND_REQUEST,
                    friendRequest.getId(),
                    sender.getNickname()
            );
        }
    }

    public void acceptFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = findAndValidateFriendRequest(requestId, memberId);

        Friend friend1 = createFriend(friendRequest.getSender(), friendRequest.getReceiver());
        Friend friend2 = createFriend(friendRequest.getReceiver(), friendRequest.getSender());

        friendRepository.save(friend1);
        friendRepository.save(friend2);

        friendRequestRepository.delete(friendRequest);

        // 알림 발송
        notificationService.createAndSendNotification(
                friendRequest.getSender().getId(),
                NotificationType.FRIEND_ACCEPT,
                friendRequest.getReceiver().getId(),
                friendRequest.getReceiver().getNickname()
        );
    }

    public void rejectFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = findAndValidateFriendRequest(requestId, memberId);

        friendRequestRepository.delete(friendRequest);
    }

    private void createFriendRelationship(Member member1, Member member2) {
        Friend friend1 = Friend.builder()
                .member(member1)
                .friend(member2)
                .build();

        Friend friend2 = Friend.builder()
                .member(member2)
                .friend(member1)
                .build();

        friendRepository.save(friend1);
        friendRepository.save(friend2);
    }

    private FriendRequest findAndValidateFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new GlobalException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!friendRequest.getReceiver().getId().equals(memberId)) {
            throw new GlobalException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        return friendRequest;
    }

    private Friend createFriend(Member member, Member friend) {
        return Friend.builder()
                .member(member)
                .friend(friend)
                .build();
    }

    private Window<FriendRequest> fetchFriendRequestWindow(Long key, int size, Long receiverId) {
        ScrollPosition scrollPosition = pagingUtil.createScrollPosition(key);
        Pageable pageable = pagingUtil.createPageable(size, DESC);

        return friendRequestRepository.findByReceiverId(receiverId, scrollPosition, pageable);
    }

    private List<FriendRequestResponse> convertToFriendRequestResponses(List<FriendRequest> requests) {
        Set<Long> senderIds = requests.stream()
                .map(request -> request.getSender().getId())
                .collect(Collectors.toSet());

        Map<Long, Member> senderMap = memberRepository.findAllById(senderIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, identity()));

        return requests.stream()
                .map(request -> {
                    Member sender = senderMap.get(request.getSender().getId());
                    if (sender == null) {
                        throw new GlobalException(ErrorCode.MEMBER_NOT_FOUND);
                    }
                    return FriendRequestResponse.builder()
                            .id(request.getId())
                            .senderId(sender.getId())
                            .nickname(sender.getNickname())
                            .profileImageUrl(sender.getProfileImageUrl())
                            .build();
                })
                .toList();
    }

}
