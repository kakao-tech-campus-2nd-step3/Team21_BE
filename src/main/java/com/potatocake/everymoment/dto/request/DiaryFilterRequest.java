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
    private Boolean isPublic;
    private int key;
    private int size;

    public List<String> getEmojis() {
        return (emoji != null && !emoji.isEmpty())
                ? Arrays.asList(emoji.split(","))
                : Collections.emptyList();
    }

    public List<String> getCategories() {
        return (category != null && !category.isEmpty())
                ? Arrays.stream(category.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public boolean hasFilter() {
        return (keyword != null && !keyword.isEmpty()) ||
                (emoji != null && !emoji.isEmpty()) ||
                (category != null && !category.isEmpty()) ||
                date != null ||
                from != null ||
                until != null ||
                isBookmark != null;
    }
}
