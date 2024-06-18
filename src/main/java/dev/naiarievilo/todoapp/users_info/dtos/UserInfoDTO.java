package dev.naiarievilo.todoapp.users_info.dtos;

import dev.naiarievilo.todoapp.validation.NotBlank;
import jakarta.annotation.Nullable;

public record UserInfoDTO(

    String email,

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @Nullable
    String avatarUrl
) { }
