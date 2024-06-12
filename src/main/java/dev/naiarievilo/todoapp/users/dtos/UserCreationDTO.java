package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.MatchingFields;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.Password;

@MatchingFields(targetField = "password", matchingField = "passwordConfirmation")
public record UserCreationDTO(

    @Email
    String email,

    @Password
    String password,

    @Password
    String passwordConfirmation,

    @NotBlank
    String firstName,

    @NotBlank
    String lastName
) {

}