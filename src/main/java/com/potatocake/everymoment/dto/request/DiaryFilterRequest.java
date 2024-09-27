package com.potatocake.everymoment.dto.request;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryFilterRequest {
    private String keyword;
    private String emoji;
    private Long category;
    private LocalDate date;
    private LocalDate from;
    private LocalDate until;
    private Boolean bookmark;
    private int key;
    private int size;
}
