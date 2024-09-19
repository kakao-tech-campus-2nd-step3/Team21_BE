package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.entity.LocationPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryManualRequestDTO {
    private List<CategoryRequestDTO> categories;
    private LocationPoint locationPoint;
    private String locationName;
    private String address;
    private boolean isBookmark;
    private boolean isPublic;
    private String emoji;
    private List<FileRequestDTO> file;
    private String content;
}

