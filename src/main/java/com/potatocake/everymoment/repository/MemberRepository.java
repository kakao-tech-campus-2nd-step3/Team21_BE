package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Member;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNumber(Long number);

    boolean existsByNumber(Long number);

    Window<Member> findByNicknameContaining(String nickname, ScrollPosition position, Pageable pageable);

    @Query(value = "SELECT CASE WHEN MIN(m.number) > 0 OR MIN(m.number) IS NULL THEN -1 ELSE MIN(m.number) - 1 END FROM member m WHERE m.deleted = 0 FOR UPDATE", nativeQuery = true)
    Long findNextAnonymousNumber();

}
