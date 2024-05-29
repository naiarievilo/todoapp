package dev.naiarievilo.todoapp.users.info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserCreationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoRepository userInfoRepository;

    public UserInfoServiceImpl(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public boolean userInfoExists(Long userId) {
        return userInfoRepository.findById(userId).isPresent();
    }

    @Override
    public UserInfo getUserInfoById(Long userId) {
        return userInfoRepository.findById(userId).orElseThrow(UserInfoNotFoundException::new);
    }

    @Override
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

    @Override
    @Transactional
    public void deleteUserInfo(Long userId) {
        if (!userInfoExists(userId))
            throw new UserInfoNotFoundException(userId);
        userInfoRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserInfo updateFirstName(UserInfo userInfo, String newFirstName) {
        Long userInfoId = userInfo.getId();
        if (!userInfoExists(userInfoId))
            throw new UserInfoNotFoundException(userInfoId);

        userInfo.setFirstName(newFirstName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo updateLastName(UserInfo userInfo, String newLastName) {
        Long userInfoId = userInfo.getId();
        if (!userInfoExists(userInfoId))
            throw new UserInfoNotFoundException(userInfoId);

        userInfo.setLastName(newLastName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo updateAvatarUrl(UserInfo userInfo, String newAvatarUrl) {
        Long userInfoId = userInfo.getId();
        if (!userInfoExists(userInfoId)) {
            throw new UserInfoNotFoundException(userInfoId);
        }

        userInfo.setAvatarUrl(newAvatarUrl);
        userInfoRepository.update(userInfo);
        return userInfo;
    }
}
