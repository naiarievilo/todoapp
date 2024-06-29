package dev.naiarievilo.todoapp.users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.users.dtos.groups.UserAuthentication;
import dev.naiarievilo.todoapp.users.dtos.groups.UserDeletion;
import dev.naiarievilo.todoapp.users.dtos.groups.UserSecurity;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Password;
import dev.naiarievilo.todoapp.validation.Positive;

public record UserDTO(

    @Positive(groups = UserDeletion.class)
    Long id,

    @Email(groups = {UserAuthentication.class, UserSecurity.class})
    String email,

    @Password(groups = UserAuthentication.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password

) {

    @Override
    public String toString() {
        return String.format("{\"id\": %d, \"email\": \"%s\", \"password\": \"%s\"}", id, email, password);
    }
}
