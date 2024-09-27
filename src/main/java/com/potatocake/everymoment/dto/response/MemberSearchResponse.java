package com.potatocake.everymoment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class MemberSearchResponse {

    private List<MemberResponse> members;
    private Long next;

}
