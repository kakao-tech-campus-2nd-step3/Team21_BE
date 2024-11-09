package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.dto.LocationPoint;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DiaryAutoCreateRequest {
    private LocationPoint locationPoint;
    private String locationName;
    private String address;
}
