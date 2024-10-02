package com.potatocake.everymoment.service;

import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.FriendRequest;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.FriendRepository;
import com.potatocake.everymoment.repository.FriendRequestRepository;
import com.potatocake.everymoment.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    public void sendFriendRequest(Long senderId, Long receiverId) {
        boolean isAlreadySend = friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiverId);

        if (isAlreadySend) {
            throw new GlobalException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        friendRequestRepository.save(FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build());
    }

    public void acceptFriendRequest(Long requestId, Long memberId) {
        FriendRequest friendRequest = findAndValidateFriendRequest(requestId, memberId);

        Friend friend1 = createFriend(friendRequest.getSender(), friendRequest.getReceiver());
        Friend friend2 = createFriend(friendRequest.getReceiver(), friendRequest.getSender());

        friendRepository.save(friend1);
        friendRepository.save(friend2);

        friendRequestRepository.delete(friendRequest);
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
                .memberId(member)
                .friendId(friend)
                .build();
    }

}
