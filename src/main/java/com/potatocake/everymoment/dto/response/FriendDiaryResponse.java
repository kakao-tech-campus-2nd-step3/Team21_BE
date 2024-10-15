package com.potatocake.everymoment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDiaryResponse {
    private Long id;
    private List<CategoryResponse> categories;
    private String locationName;
    private String emoji;
    private String content;
    private LikeCountResponse likeCount;
    private LocalDateTime createAt;
}
