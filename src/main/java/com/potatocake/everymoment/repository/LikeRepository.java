package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.Like;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Like l WHERE l.member.id = :memberId AND l.diary.id = :diaryId")
    Optional<Like> findByMemberIdAndDiaryIdWithLock(@Param("memberId") Long memberId, @Param("diaryId") Long diaryId);

    Long countByDiary(Diary diary);

    boolean existsByMemberIdAndDiaryId(Long memberId, Long diaryId);

}
