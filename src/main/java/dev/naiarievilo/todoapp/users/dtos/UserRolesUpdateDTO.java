package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.Positive;

public record UserRolesUpdateDTO(
    @Positive
    Long id,

    @NotBlank
    String role
) { }
