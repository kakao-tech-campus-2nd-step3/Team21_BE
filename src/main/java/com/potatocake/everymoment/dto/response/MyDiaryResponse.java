package com.potatocake.everymoment.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDiaryResponse {
    private Long id;
    private List<CategoryResponse> categories;
    private String address;
    private String locationName;
    private boolean isBookmark;
    private String emoji;
    private String content;
    private boolean isLiked;
    private LocalDateTime createAt;
    private LocalDate diaryDate;
}
