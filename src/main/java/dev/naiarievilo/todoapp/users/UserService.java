package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    @Override
    UserPrincipal loadUserByUsername(String username);

    void createUser(UserPrincipal user);

    void updateUser(User user);

    void deleteUser(String username);

    void changePassword(String oldPassword, String newPassword);

    boolean userExists(String username);

}
