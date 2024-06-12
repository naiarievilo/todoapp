package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.CredentialsUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.EmailUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.PasswordUpdate;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.MatchingFields;
import dev.naiarievilo.todoapp.validation.Password;

@MatchingFields(targetField = "newPassword", matchingField = "newPasswordConfirmation",
    groups = {PasswordUpdate.class, CredentialsUpdate.class})
public record CredentialsUpdateDTO(
    @Email(groups = {EmailUpdate.class, CredentialsUpdate.class})
    String newEmail,

    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String currentPassword,

    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String newPassword,

    @Password(groups = {PasswordUpdate.class, CredentialsUpdate.class})
    String newPasswordConfirmation

) {

}
