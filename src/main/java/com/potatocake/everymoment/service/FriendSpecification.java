package com.potatocake.everymoment.service;

import com.potatocake.everymoment.entity.Friend;
import com.potatocake.everymoment.entity.Member;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class FriendSpecification {
    public static Specification<Friend> filterFriends(Long memberId, String nickname) {
        return (Root<Friend> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Predicate predicate = builder.conjunction();

            predicate = builder.and(predicate, builder.equal(root.get("member").get("id"), memberId));

            if (nickname != null) {
                Join<Friend, Member> friendJoin = root.join("friend");
                predicate = builder.and(predicate, builder.like(friendJoin.get("nickname"), "%" + nickname + "%"));
            }

            return predicate;
        };
    }
}
