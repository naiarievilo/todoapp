package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.validation.PasswordMatching;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatching(password = "password", confirmPassword = "confirmPassword")
public record UserCreationDTO(
    @NotBlank
    @Size(max = 320)
    String email,

    @NotBlank
    @Size(max = 255)
    String password,

    @NotBlank
    @Size(max = 255)
    String confirmPassword,

    @NotBlank
    @Size(max = 255)
    String firstName,

    @NotBlank
    @Size(max = 255)
    String lastName
) {

}
