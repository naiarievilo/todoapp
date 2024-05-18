package dev.naiarievilo.todoapp.users;

public interface UserInfoService {

    void createUserInfo(UserCreationDTO userCreationDTO, User user);

    UserInfo changeFirstName(UserInfo userInfo, String newFirstName);

    UserInfo changeLastName(UserInfo userInfo, String newLastName);

    UserInfo changeAvatarUrl(UserInfo userInfo, String newAvatarUrl);
}
