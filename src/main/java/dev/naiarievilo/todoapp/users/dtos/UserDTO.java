package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.AuthenticateUser;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Password;
import jakarta.validation.constraints.Positive;

public record UserDTO(

    @Positive
    Integer id,

    @Email(groups = AuthenticateUser.class)
    String email,

    @Password(groups = AuthenticateUser.class)
    String password
) {

}
