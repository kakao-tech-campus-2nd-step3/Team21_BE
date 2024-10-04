package com.potatocake.everymoment.dto.response;

import com.potatocake.everymoment.entity.File;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {

    private Long id;
    private String imageUrl;
    private int order;

    public static FileResponse toResponseDto(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .imageUrl(file.getImageUrl())
                .order(file.getOrder())
                .build();
    }

}
