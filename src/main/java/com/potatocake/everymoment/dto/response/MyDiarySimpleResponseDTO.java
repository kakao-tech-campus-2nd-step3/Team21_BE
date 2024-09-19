package com.potatocake.everymoment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyDiarySimpleResponseDTO {
    private Long id;
    private String locationName;
    private String address;
    private boolean isBookmark;
    private boolean isPublic;
    private String emoji;
    private ThumbnailResponseDTO thumbnailResponseDTO;
    private String content;
    private LocalDateTime createAt;
}
