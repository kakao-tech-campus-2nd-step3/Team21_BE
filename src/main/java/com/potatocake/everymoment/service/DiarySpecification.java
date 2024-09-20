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
            if (date != null) {
                predicate = builder.and(predicate, builder.equal(builder.function("DATE", LocalDate.class, root.get("createAt")), date));
            }
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
