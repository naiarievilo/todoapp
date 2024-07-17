package dev.naiarievilo.todoapp.security.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokensDTO(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken
) { }
