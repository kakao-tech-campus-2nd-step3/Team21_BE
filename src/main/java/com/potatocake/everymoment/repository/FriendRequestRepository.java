package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.FriendRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    Window<FriendRequest> findByReceiverId(Long receiverId, ScrollPosition scrollPosition, Pageable pageable);

}
