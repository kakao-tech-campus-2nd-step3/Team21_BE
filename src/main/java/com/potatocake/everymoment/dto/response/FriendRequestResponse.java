package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FriendRequestResponse {

    private Long id;
    private Long senderId;

}
