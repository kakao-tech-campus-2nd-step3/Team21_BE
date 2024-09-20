package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.dto.LocationPoint;
import java.util.List;
import lombok.Getter;

@Getter
public class DiaryManualRequest {
    private List<CategoryRequest> categories;
    private LocationPoint locationPoint;
    private String locationName;
    private String address;
    private boolean isBookmark;
    private boolean isPublic;
    private String emoji;
    private List<FileRequest> file;
    private String content;
}

