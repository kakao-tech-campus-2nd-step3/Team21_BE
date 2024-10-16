package com.potatocake.everymoment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class LocationPoint {
    private double latitude;
    private double longitude;
}
