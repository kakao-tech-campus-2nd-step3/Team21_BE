package com.potatocake.everymoment.service;


import com.potatocake.everymoment.entity.Diary;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class DiarySpecification {

    public static Specification<Diary> filterDiaries(String keyword, String emoji, LocalDate date, LocalDate from, LocalDate until, Boolean isBookmark) {
        return (Root<Diary> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Predicate predicate = builder.conjunction();

            if (keyword != null) {
                predicate = builder.and(predicate, builder.like(root.get("content"), "%" + keyword + "%"));
            }
            if (emoji != null) {
                predicate = builder.and(predicate, builder.equal(root.get("emoji"), emoji));
            }

            // 날짜 필터링 (null일 경우 오늘 날짜로 기본값 설정)
            LocalDate filterDate = (date != null) ? date : LocalDate.now();
            predicate = builder.and(predicate, builder.equal(root.get("createAt").as(LocalDate.class), filterDate));

            if (from != null && until != null) {
                predicate = builder.and(predicate, builder.between(root.get("createAt"), from.atStartOfDay(), until.plusDays(1).atStartOfDay()));
            }
            if (isBookmark != null) {
                predicate = builder.and(predicate, builder.equal(root.get("isBookmark"), isBookmark));
            }

            return predicate;
        };
    }
}
