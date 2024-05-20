package dev.naiarievilo.todoapp.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAuthenticationDTO(
    @NotBlank(message = "Email must be provided")
    @Size(max = 320)
    String email,

    @NotBlank(message = "Password must be provided")
    @Size(max = 255)
    String password
) {

}