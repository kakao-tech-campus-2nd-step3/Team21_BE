package com.potatocake.everymoment.service;

import static java.util.function.Function.identity;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.potatocake.everymoment.dto.request.FcmNotificationRequest;
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
    private final FcmService fcmService;

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
        boolean isAlreadySend = friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId);

        if (isAlreadySend) {
            throw new GlobalException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build());

        // 알림 발송
        fcmService.sendNotification(receiverId, FcmNotificationRequest.builder()
                .title("새로운 친구 요청")
                .body(sender.getNickname() + "님이 친구 요청을 보냈습니다.")
                .type("FRIEND_REQUEST")
                .targetId(friendRequest.getId())
                .build());
    }

    public void acceptFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = findAndValidateFriendRequest(requestId, memberId);

        Friend friend1 = createFriend(friendRequest.getSender(), friendRequest.getReceiver());
        Friend friend2 = createFriend(friendRequest.getReceiver(), friendRequest.getSender());

        friendRepository.save(friend1);
        friendRepository.save(friend2);

        friendRequestRepository.delete(friendRequest);

        // 알림 발송
        fcmService.sendNotification(friendRequest.getSender().getId(), FcmNotificationRequest.builder()
                .title("친구 요청 수락")
                .body(friendRequest.getReceiver().getNickname() + "님이 친구 요청을 수락했습니다.")
                .type("FRIEND_ACCEPT")
                .targetId(friendRequest.getReceiver().getId())
                .build());
    }

    public void rejectFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = findAndValidateFriendRequest(requestId, memberId);

        friendRequestRepository.delete(friendRequest);
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
