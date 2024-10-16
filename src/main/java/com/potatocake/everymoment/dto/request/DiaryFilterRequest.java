package com.potatocake.everymoment.dto.request;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryFilterRequest {
    private String keyword;
    private String emoji;
    private String category;
    private LocalDate date;
    private LocalDate from;
    private LocalDate until;
    private Boolean isBookmark;
    private int key;
    private int size;

    public List<String> getEmojis() {
        return (emoji != null && !emoji.isEmpty())
                ? Arrays.asList(emoji.split(","))
                : Collections.emptyList();
    }

    public List<Long> getCategories() {
        return (category != null && !category.isEmpty())
                ? Arrays.stream(category.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

}
