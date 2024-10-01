package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final DiaryRepository diaryRepository;

    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(Long diaryId, Long memberId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        diary.checkOwner(memberId);

        return fileRepository.findByDiaryId(diaryId).stream()
                .map(FileResponse::toResponseDto)
                .toList();
    }

}
