package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Member;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class CategoryCreateRequest {

    @Length(max = 50)
    @NotEmpty
    private String categoryName;

    public Category toEntity(Member member) {
        return Category.builder()
                .member(member)
                .categoryName(categoryName)
                .build();
    }

}
