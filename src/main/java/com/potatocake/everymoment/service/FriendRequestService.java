package com.potatocake.everymoment.service;

import com.potatocake.everymoment.entity.FriendRequest;
import com.potatocake.everymoment.entity.Member;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
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

}
