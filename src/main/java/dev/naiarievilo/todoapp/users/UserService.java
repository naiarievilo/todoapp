package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.UserPrincipal;

public interface UserService {

    boolean userExists(String email);

    UserPrincipal loadUserByEmail(String email);

    User getUserByEmail(String email);

    User getUserByPrincipal(UserPrincipal userPrincipal);

    UserPrincipal createUser(UserCreationDTO userCreationDTO);

    void deleteUser(UserPrincipal userPrincipal);

    UserPrincipal updateEmail(UserPrincipal userPrincipal, String newEmail);

    UserPrincipal updatePassword(UserPrincipal userPrincipal, String newPassword);

    UserPrincipal addRoleToUser(UserPrincipal userPrincipal, Roles role);

    UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role);

    UserPrincipal lockUser(UserPrincipal userPrincipal);

    UserPrincipal unlockUser(UserPrincipal userPrincipal);

    UserPrincipal disableUser(UserPrincipal userPrincipal);

    UserPrincipal enableUser(UserPrincipal userPrincipal);

    void addLoginAttempt(User user);

    void resetLoginAttempts(User user);
}
