package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.PasswordMatching;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.*;
import static dev.naiarievilo.todoapp.validation.ValidationLengths.DEFAULT_MAX_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationLengths.EMAIL_MAX_LENGTH;

@PasswordMatching(password = "password", confirmPassword = "confirmPassword")
public record UserCreationDTO(
    @Email(message = EMAIL_MUST_BE_VALID)
    @NotBlank(message = EMAIL_MUST_BE_PROVIDED)
    @Size(max = EMAIL_MAX_LENGTH)
    String email,

    @NotBlank(message = PASSWORD_MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String password,

    @NotBlank(message = PASSWORD_CONFIRMATION_MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String confirmPassword,

    @NotBlank(message = FIRST_NAME_MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String firstName,

    @NotBlank(message = LAST_NAME_MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String lastName
) {

}