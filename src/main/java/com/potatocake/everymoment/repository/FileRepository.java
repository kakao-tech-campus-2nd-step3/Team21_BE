package com.potatocake.everymoment.repository;

import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.File;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByDiaryId(Long diaryId);

    void deleteByDiary(Diary diary);
    
}
