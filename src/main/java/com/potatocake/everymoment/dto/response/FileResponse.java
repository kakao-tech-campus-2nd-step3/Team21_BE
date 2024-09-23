package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {
    private Long id;
    private String imageUrl;
    private int order;
}
