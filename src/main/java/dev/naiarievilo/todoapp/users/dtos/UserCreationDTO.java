package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.MatchingFields;
import dev.naiarievilo.todoapp.validation.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static dev.naiarievilo.todoapp.validation.ValidationLengths.DEFAULT_MAX_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.MUST_BE_PROVIDED;

@MatchingFields(targetField = "password", matchingField = "confirmPassword")
public record UserCreationDTO(
    @Email
    String email,

    @Password
    String password,

    @Password
    String confirmPassword,

    @NotBlank(message = MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String firstName,

    @NotBlank(message = MUST_BE_PROVIDED)
    @Size(max = DEFAULT_MAX_LENGTH)
    String lastName
) {

}