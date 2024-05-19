package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.validation.PasswordMatching;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatching(password = "password", confirmPassword = "confirmPassword")
public record UserCreationDTO(
    @NotBlank(message = "Email must be provided")
    @Size(max = 320)
    String email,

    @NotBlank(message = "Password must be provided")
    @Size(max = 255)
    String password,

    @NotBlank(message = "Password confirmation must be provided")
    @Size(max = 255)
    String confirmPassword,

    @NotBlank(message = "First name must be provided")
    @Size(max = 255)
    String firstName,

    @NotBlank(message = "Last name must be provided")
    @Size(max = 255)
    String lastName
) {

}
