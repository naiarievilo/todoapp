package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserRepository;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.users_info.UserInfoTestCaseMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class UserInfoIntegrationTests {

    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserInfoService userInfoService;

    private UserCreationDTO userCreationDTO;
    private UserInfo userInfo;
    private User user;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        if (user == null) {
            user = loadUser(userCreationDTO);
        }

        userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUser(user);
        userInfo.setFirstName(userCreationDTO.firstName());
        userInfo.setLastName(userCreationDTO.lastName());
    }

    private User loadUser(UserCreationDTO userCreationDTO) {
        User newUser = new User();
        newUser.setEmail(userCreationDTO.email());
        newUser.setPassword(userCreationDTO.password());
        newUser.setLoginAttempts((byte) 0);
        return userRepository.persist(newUser);
    }

    @Test
    @DisplayName("userInfoExists(): " + RETURNS_FALSE_WHEN_USER_INFO_DOES_NOT_EXIST)
    void userInfoExists_UserInfoDoesNotExist_ReturnsFalse() {
        assertFalse(userInfoService.userInfoExists(userInfo.getId()));
    }

    @Test
    @Transactional
    @DisplayName("userInfoExists(): " + RETURNS_TRUE_WHEN_USER_INFO_EXISTS)
    void userInfoExists_UserInfoExists_ReturnsTrue() {
        userInfoRepository.persist(userInfo);
        Long id = userInfo.getId();

        assertTrue(userInfoService.userInfoExists(id));
        assertTrue(userInfoRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("getUserInfoById(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void getUserInfoById_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.getUserInfoById(id));
    }

    @Test
    @Transactional
    @DisplayName("getUserInfoById(): " + RETURNS_USER_INFO_WHEN_INFO_EXISTS)
    void getUserInfoById_UserInfoExists_ReturnsUserInfo() {
        userInfoRepository.persist(userInfo);

        UserInfo returnedUserInfo = userInfoService.getUserInfoById(userInfo.getId());
        assertNotNull(returnedUserInfo);
        assertEquals(userInfo.getFirstName(), returnedUserInfo.getFirstName());
        assertEquals(userInfo.getLastName(), returnedUserInfo.getLastName());
        assertEquals(userInfo.getAvatarUrl(), returnedUserInfo.getAvatarUrl());
    }

    @Test
    @Transactional
    @DisplayName("createUserInfo(): " + THROWS_USER_INFO_ALREADY_EXISTS_WHEN_INFO_ALREADY_EXISTS)
    void createUserInfo_UserInfoAlreadyExists_ThrowsUserInfoAlreadyExists() {
        userInfoRepository.persist(userInfo);
        assertThrows(UserInfoAlreadyExistsException.class, () -> userInfoService.createUserInfo(userCreationDTO, user));
    }

    @Test
    @Transactional
    @DisplayName("createUserInfo(): " + CREATES_USER_INFO_WHEN_USER_INFO_DOES_NOT_EXIST)
    void createUserInfo_UserInfoDoesNotExist_CreatesUserInfo() {
        userInfoService.createUserInfo(userCreationDTO, user);

        UserInfo createdUserInfo =
            userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(userCreationDTO.firstName(), createdUserInfo.getFirstName());
        assertEquals(userCreationDTO.lastName(), createdUserInfo.getLastName());
    }

    @Test
    @DisplayName("deleteUserInfo(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void deleteUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.deleteUserInfo(id));
    }

    @Test
    @Transactional
    @DisplayName("deleteUserInfo(): " + DELETES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void deleteUserInfo_UserInfoExists_DeletesUserInfo() {
        userInfoRepository.persist(userInfo);
        Long id = userInfo.getId();

        userInfoService.deleteUserInfo(id);
        assertTrue(userInfoRepository.findById(id).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("updateFirstName(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void updateFirstName_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateFirstName(userInfo, NEW_FIRST_NAME));
    }

    @Test
    @Transactional
    @DisplayName("updateFirstName(): " + UPDATES_FIRST_NAME_WHEN_USER_INFO_EXISTS)
    void updateFirstName_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo updatedUserInfo = userInfoService.updateFirstName(userInfo, NEW_FIRST_NAME);
        assertEquals(NEW_FIRST_NAME, updatedUserInfo.getFirstName());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(updatedUserInfo.getFirstName(), dbUserInfo.getFirstName());
    }

    @Test
    @Transactional
    @DisplayName("updateLastName(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void updateLastName_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateLastName(userInfo, NEW_LAST_NAME));
    }

    @Test
    @Transactional
    @DisplayName("updateLastName(): " + UPDATES_LAST_NAME_WHEN_USER_INFO_EXISTS)
    void updateLastName_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo updatedUserInfo = userInfoService.updateLastName(userInfo, NEW_LAST_NAME);
        assertEquals(NEW_LAST_NAME, updatedUserInfo.getLastName());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(updatedUserInfo.getLastName(), dbUserInfo.getLastName());
    }

    @Test
    @Transactional
    @DisplayName("updateAvatarUrl(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void updateAvatarUrl_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateAvatarUrl(userInfo, NEW_AVATAR_URL));
    }

    @Test
    @Transactional
    @DisplayName("updateAvatarUrl(): " + UPDATES_AVATAR_URL_WHEN_USER_INFO_EXISTS)
    void updateAvatarUrl_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo updatedUserInfo = userInfoService.updateAvatarUrl(userInfo, NEW_AVATAR_URL);
        assertEquals(NEW_AVATAR_URL, updatedUserInfo.getAvatarUrl());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(updatedUserInfo.getAvatarUrl(), dbUserInfo.getAvatarUrl());
    }
}
