package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users_info.dtos.UserInfoDTO;

public interface UserInfoService {

    boolean userInfoExists(Long userId);

    UserInfo getUserInfoById(Long userId);

    void createUserInfo(UserCreationDTO userCreationDTO, User user);

    void deleteUserInfo(Long userId);

    UserInfo updateUserInfo(Long userId, UserInfoDTO userInfoDTO);
}
