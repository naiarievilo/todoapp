package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.springframework.security.core.Authentication;

public interface UserService {

    boolean userExists(String email);

    UserPrincipal loadUserPrincipalByEmail(String email);

    User getUserByEmail(String email);

    User getUserByPrincipal(UserPrincipal userPrincipal);

    Authentication createUser(UserCreationDTO userCreationDTO);

    void deleteUser(String email);

    Authentication updateEmail(String currentEmail, String newEmail);

    Authentication updatePassword(String email, String newPassword);

    Authentication addRoleToUser(Authentication authentication, Roles role);

    Authentication removeRoleFromUser(Authentication authentication, Roles role);

    void lockUser(String email);

    void unlockUser(String email);

    void disableUser(String email);

    void enableUser(String email);

    void addLoginAttempt(User user);

    void resetLoginAttempts(User user);
}
