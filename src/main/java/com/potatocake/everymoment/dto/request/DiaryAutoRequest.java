package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.dto.LocationPoint;
import lombok.Getter;

@Getter
public class DiaryAutoRequest {
    private LocationPoint locationPoint;
    private String locationName;
    private String address;
}
