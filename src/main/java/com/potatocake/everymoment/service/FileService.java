package com.potatocake.everymoment.service;

import com.potatocake.everymoment.dto.request.FileRequest;
import com.potatocake.everymoment.dto.response.FileResponse;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.File;
import com.potatocake.everymoment.exception.ErrorCode;
import com.potatocake.everymoment.exception.GlobalException;
import com.potatocake.everymoment.repository.DiaryRepository;
import com.potatocake.everymoment.repository.FileRepository;
import com.potatocake.everymoment.util.S3FileUploader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final DiaryRepository diaryRepository;
    private final S3FileUploader uploader;

    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(Long diaryId) {
        return fileRepository.findByDiaryId(diaryId).stream()
                .map(FileResponse::toResponseDto)
                .toList();
    }

    public void uploadFiles(Long diaryId, Long memberId, List<MultipartFile> files, List<FileRequest> infos) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        diary.checkOwner(memberId);

        Map<String, MultipartFile> fileMap = files.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f, (f1, f2) -> f1));

        List<File> fileEntities = new ArrayList<>();

        for (FileRequest info : infos) {
            MultipartFile file = fileMap.get(info.getImageUrl());
            if (file == null) {
                throw new GlobalException(ErrorCode.FILE_NOT_FOUND);
            }

            String url = uploader.uploadFile(file);

            File fileEntity = File.builder()
                    .diary(diary)
                    .imageUrl(url)
                    .order(info.getOrder())
                    .build();

            fileEntities.add(fileEntity);
        }

        fileRepository.saveAll(fileEntities);
    }

    public void updateFiles(Long diaryId, Long memberId, List<MultipartFile> files, List<FileRequest> infos) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GlobalException(ErrorCode.DIARY_NOT_FOUND));

        diary.checkOwner(memberId);

        fileRepository.deleteByDiary(diary);

        uploadFiles(diaryId, memberId, files, infos);
    }

}
