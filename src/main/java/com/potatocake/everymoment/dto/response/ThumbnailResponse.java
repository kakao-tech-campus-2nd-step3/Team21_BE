package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThumbnailResponse {
    private Long id;
    private String imageUrl;
}
