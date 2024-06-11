package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.dtos.CreateUserDTO;

public interface UserInfoService {

    boolean userInfoExists(Long userId);

    UserInfo getUserInfoById(Long userId);

    void createUserInfo(CreateUserDTO createUserDTO, User user);

    void deleteUserInfo(Long userId);

    UserInfo updateFirstName(UserInfo userInfo, String newFirstName);

    UserInfo updateLastName(UserInfo userInfo, String newLastName);

    UserInfo updateAvatarUrl(UserInfo userInfo, String newAvatarUrl);
}
