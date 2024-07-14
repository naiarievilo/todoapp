package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users_info.dtos.UserInfoDTO;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoAlreadyExistsException;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;

    public UserInfoService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Transactional
    public void createUserInfo(UserCreationDTO userCreationDTO, User user) {
        Long userId = user.getId();
        if (userInfoExists(userId)) {
            throw new UserInfoAlreadyExistsException(userId);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setUser(user);
        userInfo.setFirstName(userCreationDTO.firstName());
        userInfo.setLastName(userCreationDTO.lastName());

        userInfoRepository.persist(userInfo);
    }

    public boolean userInfoExists(Long userId) {
        return userInfoRepository.findById(userId).isPresent();
    }

    @Transactional
    public void deleteUserInfo(Long userId) {
        if (!userInfoExists(userId))
            throw new UserInfoNotFoundException(userId);
        userInfoRepository.deleteById(userId);
    }

    @Transactional
    public UserInfo updateUserInfo(Long userId, UserInfoDTO userInfoDTO) {
        UserInfo userInfo = getUserInfoById(userId);
        userInfo.setFirstName(userInfoDTO.firstName());
        userInfo.setLastName(userInfoDTO.lastName());
        userInfo.setAvatarUrl(userInfoDTO.avatarUrl());

        userInfoRepository.update(userInfo);
        return userInfo;
    }

    public UserInfo getUserInfoById(Long userId) {
        return userInfoRepository.findById(userId).orElseThrow(() -> new UserInfoNotFoundException(userId));
    }
}
