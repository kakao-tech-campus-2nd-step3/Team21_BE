package com.potatocake.everymoment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberMyResponse {

    private Long id;
    private String profileImageUrl;
    private String nickname;

}
