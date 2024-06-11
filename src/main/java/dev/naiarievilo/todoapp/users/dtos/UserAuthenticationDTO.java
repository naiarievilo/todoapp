package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Password;

public record UserAuthenticationDTO(
    @Email
    String email,

    @Password
    String password
) {

}
