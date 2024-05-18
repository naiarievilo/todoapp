package dev.naiarievilo.todoapp.users;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

@Service
@Transactional(readOnly = true)
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoRepository userInfoRepository;

    public UserInfoServiceImpl(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    @Transactional
    public void createUserInfo(UserInfo userInfo) {
        Validate.notNull(userInfo, NOT_NULL.message());
        userInfoRepository.persist(userInfo);
    }

    @Override
    @Transactional
    public UserInfo changeFirstName(UserInfo userInfo, String newFirstName) {
        Validate.notNull(userInfo, NOT_NULL.message());
        Validate.notBlank(newFirstName, NOT_BLANK.message());

        userInfo.setFirstName(newFirstName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo changeLastName(UserInfo userInfo, String newLastName) {
        Validate.notNull(userInfo, NOT_NULL.message());
        Validate.notBlank(newLastName, NOT_BLANK.message());

        userInfo.setLastName(newLastName);
        userInfoRepository.update(userInfo);
        return userInfo;
    }

    @Override
    @Transactional
    public UserInfo changeAvatarUrl(UserInfo userInfo, String newAvatarUrl) {
        Validate.notNull(userInfo, NOT_NULL.message());
        Validate.notBlank(newAvatarUrl, NOT_BLANK.message());

        userInfo.setAvatarUrl(newAvatarUrl);
        userInfoRepository.update(userInfo);
        return userInfo;
    }
}
