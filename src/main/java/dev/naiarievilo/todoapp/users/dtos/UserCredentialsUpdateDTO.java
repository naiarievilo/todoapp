package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.UpdateEmail;
import dev.naiarievilo.todoapp.users.dtos.groups.UpdatePassword;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.MatchingFields;
import dev.naiarievilo.todoapp.validation.Password;

@MatchingFields(targetField = "newPassword", matchingField = "confirmNewPassword", groups = UpdatePassword.class)
public record UserCredentialsUpdateDTO(
    @Email(groups = UpdateEmail.class)
    String newEmail,

    @Password(groups = UpdatePassword.class)
    String currentPassword,

    @Password(groups = UpdatePassword.class)
    String newPassword,

    @Password(groups = UpdatePassword.class)
    String confirmNewPassword
) {

}
