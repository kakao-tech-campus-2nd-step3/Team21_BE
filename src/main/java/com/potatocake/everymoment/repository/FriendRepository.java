package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FriendRepository extends JpaRepository<Friend, Long>, JpaSpecificationExecutor<Friend> {
    Optional<Friend> findByMemberIdAndFriendId(Member member, Member friend);

    List<Member> findAllFriendIdsByMemberId(Member member);
}
