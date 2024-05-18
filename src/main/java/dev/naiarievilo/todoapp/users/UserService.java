package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.UserPrincipal;

public interface UserService {

    boolean userExists(String email);

    UserPrincipal loadUserByEmail(String email);

    User getUser(UserPrincipal userPrincipal);

    UserPrincipal createUser(UserCreationDTO userCreationDTO);

    void deleteUser(UserPrincipal userPrincipal);

    UserPrincipal changeEmail(UserPrincipal userPrincipal, String newEmail);

    UserPrincipal changePassword(UserPrincipal userPrincipal, String newPassword);

    UserPrincipal addRoleToUser(UserPrincipal userPrincipal, Roles role);

    UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role);

    void lockUser(UserPrincipal userPrincipal);

    void unlockUser(UserPrincipal userPrincipal);

    void disableUser(UserPrincipal userPrincipal);

    void enableUser(UserPrincipal userPrincipal);
}
