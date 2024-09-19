package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.DiaryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryCategoryRepository extends JpaRepository<DiaryCategory, Long> {
    List<DiaryCategory> findByCategoryId(Long categoryId);
}
