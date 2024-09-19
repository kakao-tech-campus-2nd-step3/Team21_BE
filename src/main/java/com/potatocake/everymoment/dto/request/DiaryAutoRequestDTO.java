package com.potatocake.everymoment.dto.request;

import com.potatocake.everymoment.entity.LocationPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAutoRequestDTO {
    private LocationPoint locationPoint;
    private String locationName;
    private String address;
}
