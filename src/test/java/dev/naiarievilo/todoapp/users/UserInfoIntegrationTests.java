package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.users.info.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(readOnly = true)
class UserInfoIntegrationTests {

    private static final String NEW_AVATAR_URL = "path/to/new/avatar/url";
    private static final String NEW_FIRST_NAME = "newFirstName";
    private static final String NEW_LAST_NAME = "newLastName";
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
        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);

        if (user == null)
            user = loadUser(userCreationDTO);

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
        newUser.setFailedLoginAttempts(0);
        return userRepository.persist(newUser);
    }

    @Test
    @DisplayName("userInfoExists(): Returns `false` when user info doesn't exist")
    void userInfoExists_UserInfoDoesNotExist_ReturnsFalse() {
        assertFalse(userInfoService.userInfoExists(ID));
        assertTrue(userInfoRepository.findById(ID).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("userInfoExists(): Returns `true` when user info exists")
    void userInfoExists_UserInfoExists_ReturnsTrue() {
        userInfoRepository.persist(userInfo);
        Long userInfoId = userInfo.getId();

        assertTrue(userInfoService.userInfoExists(userInfoId));
        assertTrue(userInfoRepository.findById(userInfoId).isPresent());
    }

    @Test
    @DisplayName("getUserInfoById(): Throws `UserInfoNotFoundException` when user info doesn't exist")
    void getUserInfoById_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.getUserInfoById(ID));
        assertTrue(userInfoRepository.findById(ID).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("getUserInfoById(): Returns `UserInfo` when user info exists")
    void getUserInfoById_UserInfoExists_ReturnsUserInfo() {
        userInfoRepository.persist(userInfo);
        UserInfo returnedUserInfo = userInfoService.getUserInfoById(userInfo.getId());
        assertThat(returnedUserInfo).isNotNull();
        assertEquals(userInfo.getFirstName(), returnedUserInfo.getFirstName());
        assertEquals(userInfo.getLastName(), returnedUserInfo.getLastName());
        assertEquals(userInfo.getAvatarUrl(), returnedUserInfo.getAvatarUrl());
    }

    @Test
    @Transactional
    @DisplayName("createUserInfo(): Throws `UserInfoAlreadyExistsException` when user info already exists")
    void createUserInfo_UserInfoAlreadyExists_ThrowsUserInfoAlreadyExists() {
        userInfoRepository.persist(userInfo);
        assertThrows(UserInfoAlreadyExistsException.class, () -> userInfoService.createUserInfo(userCreationDTO, user));
        assertTrue(userInfoRepository.findById(user.getId()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("createUserInfo(): Creates user info when user info doesn't exist")
    void createUserInfo_UserInfoDoesNotExist_CreatesUserInfo() {
        userInfoService.createUserInfo(userCreationDTO, user);

        Optional<UserInfo> createdUserInfo = userInfoRepository.findById(userInfo.getId());
        assertTrue(createdUserInfo.isPresent());
        UserInfo userInfoToCheck = createdUserInfo.get();
        assertEquals(userCreationDTO.firstName(), userInfoToCheck.getFirstName());
        assertEquals(userCreationDTO.lastName(), userInfoToCheck.getLastName());
        assertNull(userInfoToCheck.getAvatarUrl());
    }

    @Test
    @DisplayName("deleteUserInfo(): Throws `UserInfoNotFoundException` when user info doesn't exist")
    void deleteUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long userId = user.getId();
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.deleteUserInfo(userId));
        assertTrue(userInfoRepository.findById(userId).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("deleteUserInfo(): Deletes user info when user info exists")
    void deleteUserInfo_UserInfoExists_DeletesUserInfo() {
        userInfoRepository.persist(userInfo);
        userInfoService.deleteUserInfo(userInfo.getId());
        assertTrue(userInfoRepository.findById(userInfo.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("updateFirstName(): Throws `UserInfoNotFoundException` when user doesn't exist")
    void updateFirstName_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateFirstName(userInfo, NEW_FIRST_NAME));
        assertTrue(userInfoRepository.findById(userInfo.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("updateFirstName(): Updates first name when user info exists")
    void updateFirstName_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo returnedUserInfo = userInfoService.updateFirstName(userInfo, NEW_FIRST_NAME);
        assertEquals(NEW_FIRST_NAME, returnedUserInfo.getFirstName());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(returnedUserInfo.getFirstName(), dbUserInfo.getFirstName());
    }

    @Test
    @Transactional
    @DisplayName("updateLastName(): Throws `UserInfoNotFoundException` when user doesn't exist")
    void updateLastName_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateLastName(userInfo, NEW_LAST_NAME));
        assertTrue(userInfoRepository.findById(userInfo.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("updateLastName(): Updates first name when user info exists")
    void updateLastName_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo returnedUserInfo = userInfoService.updateLastName(userInfo, NEW_LAST_NAME);
        assertEquals(NEW_LAST_NAME, returnedUserInfo.getLastName());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(returnedUserInfo.getLastName(), dbUserInfo.getLastName());
    }

    @Test
    @Transactional
    @DisplayName("updateAvatarUrl(): Throws `UserInfoNotFoundException` when user doesn't exist")
    void updateAvatarUrl_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateAvatarUrl(userInfo, NEW_AVATAR_URL));
        assertTrue(userInfoRepository.findById(userInfo.getId()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("updateAvatarUrl(): Updates first name when user info exists")
    void updateAvatarUrl_UserInfoExists_UpdatesFirstName() {
        userInfoRepository.persist(userInfo);

        UserInfo returnedUserInfo = userInfoService.updateAvatarUrl(userInfo, NEW_AVATAR_URL);
        assertEquals(NEW_AVATAR_URL, returnedUserInfo.getAvatarUrl());

        UserInfo dbUserInfo = userInfoRepository.findById(userInfo.getId()).orElseThrow(UserInfoNotFoundException::new);
        assertEquals(returnedUserInfo.getAvatarUrl(), dbUserInfo.getAvatarUrl());
    }
}
