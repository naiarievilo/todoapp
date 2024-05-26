package dev.naiarievilo.todoapp.users;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_NULL;

@Service
@Transactional(readOnly = true)
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoRepository userInfoRepository;

    public UserInfoServiceImpl(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public UserInfo getUserInfoById(Long userId) {
        Validate.notNull(userId, NOT_NULL);
        return userInfoRepository.findById(userId).orElseThrow(UserInfoNotFoundException::new);
    }

    @Override
    @Transactional
    public void createUserInfo(UserCreationDTO userCreationDTO, User user) {
        Validate.notNull(userCreationDTO, NOT_NULL);
        Validate.notNull(user, NOT_NULL);

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
        Validate.notNull(userId, NOT_NULL);
        userInfoRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserInfo changeFirstName(UserInfo userInfo, String newFirstName) {
        Validate.notNull(userInfo, NOT_NULL);
        Validate.notBlank(newFirstName, NOT_BLANK);

        userInfo.setFirstName(newFirstName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo changeLastName(UserInfo userInfo, String newLastName) {
        Validate.notNull(userInfo, NOT_NULL);
        Validate.notBlank(newLastName, NOT_BLANK);

        userInfo.setLastName(newLastName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo changeAvatarUrl(UserInfo userInfo, String newAvatarUrl) {
        Validate.notNull(userInfo, NOT_NULL);
        Validate.notBlank(newAvatarUrl, NOT_BLANK);

        userInfo.setAvatarUrl(newAvatarUrl);
        userInfoRepository.update(userInfo);
        return userInfo;
    }
}
