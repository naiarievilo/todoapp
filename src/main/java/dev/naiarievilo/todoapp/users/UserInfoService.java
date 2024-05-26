package dev.naiarievilo.todoapp.users;

public interface UserInfoService {

    boolean userInfoExists(Long userId);

    UserInfo getUserInfoById(Long userId);

    void createUserInfo(UserCreationDTO userCreationDTO, User user);

    void deleteUserInfo(Long userId);

    UserInfo updateFirstName(UserInfo userInfo, String newFirstName);

    UserInfo updateLastName(UserInfo userInfo, String newLastName);

    UserInfo updateAvatarUrl(UserInfo userInfo, String newAvatarUrl);
}
