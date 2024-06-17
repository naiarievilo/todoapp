package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.UserAuthentication;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Id;
import dev.naiarievilo.todoapp.validation.Password;

public record UserDTO(

    @Id
    Long id,

    @Email(groups = UserAuthentication.class)
    String email,

    @Password(groups = UserAuthentication.class)
    String password
) {

}
