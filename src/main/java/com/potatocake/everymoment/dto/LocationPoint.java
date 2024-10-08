package com.potatocake.everymoment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LocationPoint {
    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        return latitude + "/" + longitude;
    }
}
