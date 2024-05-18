package dev.naiarievilo.todoapp.users;

public interface UserInfoService {

    UserInfo getUserInfoById(Long userId);

    void createUserInfo(UserCreationDTO userCreationDTO, User user);

    void deleteUserInfo(Long userId);

    UserInfo changeFirstName(UserInfo userInfo, String newFirstName);

    UserInfo changeLastName(UserInfo userInfo, String newLastName);

    UserInfo changeAvatarUrl(UserInfo userInfo, String newAvatarUrl);
}
