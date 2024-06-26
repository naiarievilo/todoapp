package dev.naiarievilo.todoapp.users.dtos;

import dev.naiarievilo.todoapp.users.dtos.groups.UserAuthentication;
import dev.naiarievilo.todoapp.users.dtos.groups.UserDeletion;
import dev.naiarievilo.todoapp.users.dtos.groups.UserSecurity;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Id;
import dev.naiarievilo.todoapp.validation.Password;

public record UserDTO(

    @Id(groups = UserDeletion.class)
    Long id,

    @Email(groups = {UserAuthentication.class, UserSecurity.class})
    String email,

    @Password(groups = UserAuthentication.class)
    String password
) {

}
