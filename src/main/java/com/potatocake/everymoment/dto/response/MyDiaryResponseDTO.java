package com.potatocake.everymoment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyDiaryResponseDTO {
    private Long id;
    private List<CategoryResponseDTO> categories;
    private String address;
    private String locationName;
    private boolean isBookmark;
    private String emoji;
    private List<FileResponseDTO> file;
    private String content;
    private LocalDateTime createAt;
}
