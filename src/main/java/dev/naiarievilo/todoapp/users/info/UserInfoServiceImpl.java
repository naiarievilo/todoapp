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
        if (userInfoExists(user.getId())) {
            throw new UserInfoAlreadyExistsException();
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUser(user);
        userInfo.setFirstName(userCreationDTO.firstName());
        userInfo.setLastName(userCreationDTO.lastName());

        userInfoRepository.persist(userInfo);
    }

    @Override
    @Transactional
    public void deleteUserInfo(Long userId) {
        if (!userInfoExists(userId))
            throw new UserInfoNotFoundException();
        userInfoRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserInfo updateFirstName(UserInfo userInfo, String newFirstName) {
        if (!userInfoExists(userInfo.getId()))
            throw new UserInfoNotFoundException();

        userInfo.setFirstName(newFirstName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo updateLastName(UserInfo userInfo, String newLastName) {
        if (!userInfoExists(userInfo.getId()))
            throw new UserInfoNotFoundException();

        userInfo.setLastName(newLastName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo updateAvatarUrl(UserInfo userInfo, String newAvatarUrl) {
        if (!userInfoExists(userInfo.getId())) {
            throw new UserInfoNotFoundException();
        }

        userInfo.setAvatarUrl(newAvatarUrl);
        userInfoRepository.update(userInfo);
        return userInfo;
    }
}
