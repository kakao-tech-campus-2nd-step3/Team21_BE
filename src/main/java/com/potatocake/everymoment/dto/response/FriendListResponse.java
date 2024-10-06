package com.potatocake.everymoment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class FriendListResponse {
    private List<FriendProfileResponse> friends;
    private Integer next;
}
