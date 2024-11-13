package com.potatocake.everymoment.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDiarySimpleResponse {
    private Long id;
    private String locationName;
    private String address;
    private boolean isBookmark;
    private boolean isPublic;
    private String emoji;
    private ThumbnailResponse thumbnailResponse;
    private String content;
    private LocalDateTime createAt;
    private LocalDate diaryDate;
}
