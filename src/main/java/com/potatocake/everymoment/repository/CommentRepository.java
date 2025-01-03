package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByDiaryId(Long diaryId, Pageable pageable);

    Long countByDiaryId(Long diaryId);
}
