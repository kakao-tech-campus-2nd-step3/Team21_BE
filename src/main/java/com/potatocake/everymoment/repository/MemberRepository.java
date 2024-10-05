package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Member;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNumber(Long number);

    boolean existsByNumber(Long number);

    Window<Member> findByNicknameContaining(String nickname, ScrollPosition position, Pageable pageable);

}
