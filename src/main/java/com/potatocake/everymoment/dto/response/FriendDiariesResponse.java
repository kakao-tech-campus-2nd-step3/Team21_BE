package com.potatocake.everymoment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendDiariesResponse {
    private List<FriendDiarySimpleResponse> diaries;
    private Integer next;
}
