package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDiarySimpleResponse {
    private Long id;
    private String locationName;
    private String address;
    private boolean isBookmark;
    private boolean isPublic;
    private String emoji;
    private ThumbnailResponse thumbnailResponse;
    private String content;
    private LocalDateTime createAt;
}
