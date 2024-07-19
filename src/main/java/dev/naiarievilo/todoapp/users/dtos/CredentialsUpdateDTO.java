package dev.naiarievilo.todoapp.users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.users.dtos.groups.CredentialsUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.EmailUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.PasswordUpdate;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.MatchingFields;
import dev.naiarievilo.todoapp.validation.Password;

@MatchingFields(targetField = "newPassword", matchingField = "newPasswordConfirmation",
    groups = {PasswordUpdate.class, CredentialsUpdate.class})
public record CredentialsUpdateDTO(
    @JsonProperty("new_email")
    @Email(groups = {EmailUpdate.class, CredentialsUpdate.class})
    String newEmail,

    @JsonProperty("current_password")
    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String currentPassword,

    @JsonProperty("new_password")
    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String newPassword,

    @JsonProperty("confirm_new_password")
    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String newPasswordConfirmation

) {

}
