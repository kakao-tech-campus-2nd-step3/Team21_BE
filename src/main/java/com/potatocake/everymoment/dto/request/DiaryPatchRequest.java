package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.dto.LocationPoint;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class DiaryPatchRequest {

    private List<CategoryRequest> categories;

    @Size(max = 50, message = "장소명은 50자를 초과할 수 없습니다")
    private String locationName;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    private String address;

    @Size(max = 10, message = "이모지는 10자를 초과할 수 없습니다")
    private String emoji;

    @Size(max = 5000, message = "일기 내용은 5000자를 초과할 수 없습니다")
    private String content;

}
