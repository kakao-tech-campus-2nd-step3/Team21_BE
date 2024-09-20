package com.potatocake.everymoment.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class JwtResponse {

    private String token;

    public static JwtResponse of(String token) {
        return new JwtResponse(token);
    }

}
