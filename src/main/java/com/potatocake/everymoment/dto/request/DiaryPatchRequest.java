package com.potatocake.everymoment.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DiaryPatchRequest {

    private Boolean deleteAllCategories;
    private Boolean emojiDelete;
    private Boolean contentDelete;

    private List<CategoryRequest> categories;

    @Size(max = 50, message = "장소명은 50자를 초과할 수 없습니다")
    private String locationName;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    private String address;

    @Size(max = 10, message = "이모지는 10자를 초과할 수 없습니다")
    private String emoji;

    @Size(max = 15000, message = "일기 내용은 15,000자를 초과할 수 없습니다")
    private String content;

}
