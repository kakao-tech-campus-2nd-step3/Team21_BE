package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long>, JpaSpecificationExecutor<Diary> {
}