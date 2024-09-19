package com.potatocake.everymoment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyDiariesResponseDTO {
    private List<MyDiarySimpleResponseDTO> diaries;
    private Integer key;
}
