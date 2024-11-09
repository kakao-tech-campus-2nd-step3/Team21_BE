package com.potatocake.everymoment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class AnonymousLoginResponse {

    private Long number;
    private String token;

}
