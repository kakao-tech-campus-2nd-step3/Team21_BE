package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private long id;
    private String categoryName;
}