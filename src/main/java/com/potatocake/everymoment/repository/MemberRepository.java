package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
