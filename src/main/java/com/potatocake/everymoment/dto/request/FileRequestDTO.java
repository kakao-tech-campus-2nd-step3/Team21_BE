package com.potatocake.everymoment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileRequestDTO {
    private String imageUrl;
    private int order;
}
