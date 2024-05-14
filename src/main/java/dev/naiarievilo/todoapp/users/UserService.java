package dev.naiarievilo.todoapp.users;

public interface UserService {

    UserPrincipal loadUserByEmail(String email);

    User getUser(String email);

    void createUser(UserPrincipal user);

    void updateUser(UserPrincipal user);

    void deleteUser(UserPrincipal user);

    void changePassword(String oldPassword, String newPassword);

    boolean userExists(String username);

}
