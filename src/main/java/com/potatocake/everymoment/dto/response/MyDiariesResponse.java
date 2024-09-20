package com.potatocake.everymoment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDiariesResponse {
    private List<MyDiarySimpleResponse> diaries;
    private Integer key;
}
