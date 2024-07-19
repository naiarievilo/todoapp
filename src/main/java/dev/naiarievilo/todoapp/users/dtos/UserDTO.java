package dev.naiarievilo.todoapp.users.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.users.dtos.groups.UserAuthentication;
import dev.naiarievilo.todoapp.users.dtos.groups.UserDeletion;
import dev.naiarievilo.todoapp.users.dtos.groups.UserSecurity;
import dev.naiarievilo.todoapp.validation.Email;
import dev.naiarievilo.todoapp.validation.Password;
import dev.naiarievilo.todoapp.validation.Positive;

public class UserDTO {

    @Positive(groups = UserDeletion.class)
    private final Long id;

    @Email(groups = {UserAuthentication.class, UserSecurity.class})
    private final String email;

    @Password(groups = UserAuthentication.class)
    private final String password;

    private final Boolean verified;

    @JsonCreator
    public UserDTO(
        @JsonProperty("id")
        Long id,
        @JsonProperty("email")
        String email,
        @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
        String password,
        @JsonProperty(value = "is_verified", access = JsonProperty.Access.READ_ONLY)
        Boolean verified
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.verified = verified;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getVerified() {
        return verified;
    }

    @Override
    public String toString() {
        return String.format("{\"id\": %d, \"email\": \"%s\", \"password\": \"%s\"}", id, email, password);
    }
}