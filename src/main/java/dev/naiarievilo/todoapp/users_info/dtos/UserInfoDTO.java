package dev.naiarievilo.todoapp.users_info.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.validation.NotBlank;
import jakarta.annotation.Nullable;

public record UserInfoDTO(

    String email,

    @JsonProperty("first_name")
    @NotBlank
    String firstName,

    @JsonProperty("last_name")
    @NotBlank
    String lastName,

    @JsonProperty("avatar_url")
    @Nullable
    String avatarUrl
) { }
