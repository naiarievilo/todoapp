package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import org.springframework.security.core.Authentication;

public interface UserService {

    boolean userExists(String email);

    UserPrincipal loadUserPrincipalByEmail(String email);

    User getUserByEmail(String email);

    User getUserByPrincipal(UserPrincipal userPrincipal);

    Authentication createUser(UserCreationDTO userCreationDTO);

    void deleteUser(String email);

    Authentication updateEmail(String oldEmail, String newEmail);

    Authentication updatePassword(String email, String newPassword);

    Authentication addRoleToUser(Authentication authentication, Roles role);

    Authentication removeRoleFromUser(Authentication authentication, Roles role);

    UserPrincipal lockUser(UserPrincipal userPrincipal);

    UserPrincipal unlockUser(UserPrincipal userPrincipal);

    UserPrincipal disableUser(UserPrincipal userPrincipal);

    UserPrincipal enableUser(UserPrincipal userPrincipal);

    void addLoginAttempt(User user);

    void resetLoginAttempts(User user);
}
