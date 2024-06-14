package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;

public interface UserService {

    boolean isUserInactive(User user);

    boolean isUserExpired(User user);

    boolean userExists(Long id);

    boolean userExists(String email);

    User getUserByEmail(String email);

    User getUserById(Long id);

    User createUser(UserCreationDTO userCreationDTO);

    void deleteUser(User user);

    User updateEmail(User user, String newEmail);

    User updatePassword(User user, String currentPassword, String newPassword);

    User addRoleToUser(User user, Roles roleToAdd);

    User removeRoleFromUser(User user, Roles role);

    User lockUser(User user);

    User unlockUser(User user);

    User disableUser(User user);

    User enableUser(User user);

    void addLoginAttempt(User user);

    void resetLoginAttempts(User user);
}
