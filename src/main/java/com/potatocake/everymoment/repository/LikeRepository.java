package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByMemberIdAndDiaryId(Long memberId, Long diaryId);

}
