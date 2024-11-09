package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.entity.Category;
import com.potatocake.everymoment.entity.Member;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CategoryCreateRequest {

    @Size(max = 50, message = "카테고리명은 50자를 초과할 수 없습니다")
    @NotEmpty(message = "카테고리명은 필수입니다")
    private String categoryName;

    public Category toEntity(Member member) {
        return Category.builder()
                .member(member)
                .categoryName(categoryName)
                .build();
    }

}
