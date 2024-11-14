package com.potatocake.everymoment.service;


import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Diary;
import com.potatocake.everymoment.entity.DiaryCategory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DiarySpecification {

    public static Specification<Diary> filterDiaries(
            String keyword, List<String> emojis, List<String> categories,
            LocalDate date, LocalDate from, LocalDate until, Boolean isBookmark, Boolean isPublic) {
        return (Root<Diary> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Predicate predicate = builder.conjunction();

            if (keyword != null) {
                predicate = builder.and(predicate, builder.like(root.get("content"), "%" + keyword + "%"));
            }

            if (emojis != null && !emojis.isEmpty()) {
                predicate = builder.and(predicate, root.get("emoji").in(emojis));
            }

            if (categories != null && !categories.isEmpty()) {
                Join<Diary, DiaryCategory> diaryCategoryJoin = root.join("diaryCategories", JoinType.LEFT);
                Join<DiaryCategory, Category> categoryJoin = diaryCategoryJoin.join("category", JoinType.LEFT);
                predicate = builder.and(predicate, categoryJoin.get("categoryName").in(categories));
            }

            if (date != null) {
                predicate = builder.and(predicate,
                        builder.or(
                                builder.and(
                                        builder.isNotNull(root.get("diaryDate")),
                                        builder.equal(root.get("diaryDate"), date)
                                ),
                                builder.and(
                                        builder.isNull(root.get("diaryDate")),
                                        builder.equal(root.get("createAt").as(LocalDate.class), date)
                                )
                        ));
            }

            if (from != null && until != null) {
                predicate = builder.and(predicate,
                        builder.or(
                                builder.and(
                                        builder.isNotNull(root.get("diaryDate")),
                                        builder.between(root.get("diaryDate"), from, until)
                                ),
                                builder.and(
                                        builder.isNull(root.get("diaryDate")),
                                        builder.between(root.get("createAt"), from, until.plusDays(1))
                                )
                        ));
            }

            if (isBookmark != null) {
                predicate = builder.and(predicate, builder.equal(root.get("isBookmark"), isBookmark));
            }

            if (isPublic != null) {
                predicate = builder.and(predicate, builder.equal(root.get("isPublic"), isPublic));
            }

            return predicate;
        };
    }
}
