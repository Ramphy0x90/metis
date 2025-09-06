package com.r16a.metis.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String username;
    private String name;
    private String surname;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}

