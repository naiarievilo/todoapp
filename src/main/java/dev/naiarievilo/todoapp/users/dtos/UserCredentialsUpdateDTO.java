package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.UpdateEmail;
import dev.naiarievilo.todoapp.users.dtos.groups.UpdatePassword;
import dev.naiarievilo.todoapp.validation.PasswordMatching;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.*;
import static dev.naiarievilo.todoapp.validation.ValidationLengths.DEFAULT_MAX_LENGTH;
import static dev.naiarievilo.todoapp.validation.ValidationLengths.EMAIL_MAX_LENGTH;

@PasswordMatching(password = "newPassword", confirmPassword = "confirmNewPassword", groups = UpdatePassword.class)
public record UserCredentialsUpdateDTO(
    @NotBlank(groups = UpdateEmail.class, message = EMAIL_MUST_BE_PROVIDED)
    @Email(groups = UpdateEmail.class, message = EMAIL_MUST_BE_VALID)
    @Size(groups = UpdateEmail.class, max = EMAIL_MAX_LENGTH)
    String email,

    @NotBlank(groups = UpdatePassword.class, message = CURRENT_PASSWORD_MUST_BE_PROVIDED)
    @Size(groups = UpdatePassword.class, max = DEFAULT_MAX_LENGTH)
    String currentPassword,

    @NotBlank(groups = UpdatePassword.class, message = NEW_PASSWORD_MUST_BE_PROVIDED)
    @Size(groups = UpdatePassword.class, max = DEFAULT_MAX_LENGTH)
    String newPassword,

    @NotBlank(groups = UpdatePassword.class, message = PASSWORD_CONFIRMATION_MUST_BE_PROVIDED)
    @Size(groups = UpdatePassword.class, max = DEFAULT_MAX_LENGTH)
    String confirmNewPassword
) {

}
