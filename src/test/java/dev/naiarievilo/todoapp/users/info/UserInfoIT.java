package dev.naiarievilo.todoapp.users.info;

import dev.naiarievilo.todoapp.ServiceIntegrationTests;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserRepository;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.info.dtos.UserInfoDTO;
import dev.naiarievilo.todoapp.users.info.exceptions.UserInfoAlreadyExistsException;
import dev.naiarievilo.todoapp.users.info.exceptions.UserInfoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class UserInfoIT extends ServiceIntegrationTests {

    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserInfoService userInfoService;

    private UserCreationDTO userCreationDTO;
    private UserInfo userInfo;
    private User user;
    private UserInfoDTO userInfoDTO;

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

        userInfoDTO = new UserInfoDTO(null, NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR_URL);
    }

    private User loadUser(UserCreationDTO userCreationDTO) {
        User newUser = new User();
        newUser.setEmail(userCreationDTO.email());
        newUser.setPassword(userCreationDTO.password());
        newUser.setLoginAttempts((byte) 0);
        return userRepository.persist(newUser);
    }

    @Test
    @DisplayName("userInfoExists(): " + UserInfoTestCases.RETURNS_FALSE_WHEN_USER_INFO_DOES_NOT_EXIST)
    void userInfoExists_UserInfoDoesNotExist_ReturnsFalse() {
        assertFalse(userInfoService.userInfoExists(userInfo.getId()));
    }

    @Test
    @DisplayName("userInfoExists(): " + UserInfoTestCases.RETURNS_TRUE_WHEN_USER_INFO_EXISTS)
    void userInfoExists_UserInfoExists_ReturnsTrue() {
        userInfoRepository.persist(userInfo);
        Long id = userInfo.getId();

        assertTrue(userInfoService.userInfoExists(id));
        assertTrue(userInfoRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("getUserInfoById(): " + UserInfoTestCases.THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void getUserInfoById_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.getUserInfoById(id));
    }

    @Test
    @DisplayName("getUserInfoById(): " + UserInfoTestCases.RETURNS_USER_INFO_WHEN_INFO_EXISTS)
    void getUserInfoById_UserInfoExists_ReturnsUserInfo() {
        userInfoRepository.persist(userInfo);

        UserInfo returnedUserInfo = userInfoService.getUserInfoById(userInfo.getId());
        assertNotNull(returnedUserInfo);
        assertEquals(userInfo.getFirstName(), returnedUserInfo.getFirstName());
        assertEquals(userInfo.getLastName(), returnedUserInfo.getLastName());
        assertEquals(userInfo.getAvatarUrl(), returnedUserInfo.getAvatarUrl());
    }

    @Test
    @DisplayName("createUserInfo(): " + UserInfoTestCases.THROWS_USER_INFO_ALREADY_EXISTS_WHEN_INFO_ALREADY_EXISTS)
    void createUserInfo_UserInfoAlreadyExists_ThrowsUserInfoAlreadyExists() {
        userInfoRepository.persist(userInfo);
        assertThrows(UserInfoAlreadyExistsException.class, () -> userInfoService.createUserInfo(userCreationDTO, user));
    }

    @Test
    @DisplayName("createUserInfo(): " + UserInfoTestCases.CREATES_USER_INFO_WHEN_USER_INFO_DOES_NOT_EXIST)
    void createUserInfo_UserInfoDoesNotExist_CreatesUserInfo() {
        userInfoService.createUserInfo(userCreationDTO, user);

        UserInfo createdUserInfo =
            userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(userCreationDTO.firstName(), createdUserInfo.getFirstName());
        assertEquals(userCreationDTO.lastName(), createdUserInfo.getLastName());
    }

    @Test
    @DisplayName("deleteUserInfo(): " + UserInfoTestCases.THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void deleteUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.deleteUserInfo(id));
    }

    @Test
    @DisplayName("deleteUserInfo(): " + UserInfoTestCases.DELETES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void deleteUserInfo_UserInfoExists_DeletesUserInfo() {
        userInfoRepository.persist(userInfo);
        Long id = userInfo.getId();

        userInfoService.deleteUserInfo(id);
        assertTrue(userInfoRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("updateUserInfo(): " + UserInfoTestCases.THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void updateUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = user.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateUserInfo(id, userInfoDTO));
    }

    @Test
    @DisplayName("updateUserInfo(): " + UserInfoTestCases.UPDATES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void updateUserInfo_UserInfoExists_UpdatesUserInfo() {
        Long id = user.getId();
        userInfoRepository.persist(userInfo);

        UserInfo updatedUserInfo = userInfoService.updateUserInfo(id, userInfoDTO);
        assertEquals(updatedUserInfo.getFirstName(), userInfoDTO.firstName());
        assertEquals(updatedUserInfo.getLastName(), userInfoDTO.lastName());
        assertEquals(updatedUserInfo.getAvatarUrl(), userInfoDTO.avatarUrl());

        UserInfo userInfoUpdated = userInfoRepository.findById(id).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(userInfoUpdated.getFirstName(), userInfoDTO.firstName());
        assertEquals(userInfoUpdated.getLastName(), userInfoDTO.lastName());
        assertEquals(userInfoUpdated.getAvatarUrl(), userInfoDTO.avatarUrl());
    }

}