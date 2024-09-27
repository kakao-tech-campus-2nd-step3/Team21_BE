package com.potatocake.everymoment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendListResponse {
    private List<FriendProfileResponse> friends;
    private Integer next;
}
