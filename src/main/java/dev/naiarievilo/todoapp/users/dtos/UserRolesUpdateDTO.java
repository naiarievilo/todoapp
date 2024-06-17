package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.Id;
import dev.naiarievilo.todoapp.validation.NotBlank;

public record UserRolesUpdateDTO(
    @Id
    Long id,

    @NotBlank
    String role
) { }
