package dev.naiarievilo.todoapp.users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("password_confirmation")
    @Password
    String passwordConfirmation,

    @JsonProperty("first_name")
    @NotBlank
    String firstName,

    @JsonProperty("last_name")
    @NotBlank
    String lastName
) {

}