package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.validation.NotBlank;
import jakarta.annotation.Nullable;

public record UserInfoDTO(

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @Nullable
    String avatarUrl
) {

}
