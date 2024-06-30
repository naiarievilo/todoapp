package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users_info.dtos.UserInfoDTO;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoAlreadyExistsException;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.users_info.UserInfoTestCases.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserInfoUnitTests {

    @Mock
    UserInfoRepository userInfoRepository;
    @InjectMocks
    UserInfoServiceImpl userInfoService;

    @Captor
    private ArgumentCaptor<UserInfo> userInfoCaptor;
    private UserInfo userInfo;
    private User user;
    private UserCreationDTO userCreationDTO;
    private UserInfoDTO userInfoDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

        user = new User();
        user.setId(ID_1);

        userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUser(user);
        userInfo.setFirstName(userCreationDTO.firstName());
        userInfo.setLastName(userCreationDTO.lastName());

        userInfoDTO = new UserInfoDTO(null, NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR_URL);
    }

    @Test
    @DisplayName("userInfoExists(): " + RETURNS_FALSE_WHEN_USER_INFO_DOES_NOT_EXIST)
    void userInfoExists_UserInfoDoesNotExist_ReturnsFalse() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.empty());

        assertFalse(userInfoService.userInfoExists(id));
        verify(userInfoRepository).findById(id);
    }

    @Test
    @DisplayName("userInfoExists(): " + RETURNS_TRUE_WHEN_USER_INFO_EXISTS)
    void userInfoExists_UserInfoExists_ReturnsTrue() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.of(userInfo));

        assertTrue(userInfoService.userInfoExists(id));
        verify(userInfoRepository).findById(id);
    }

    @Test
    @DisplayName("getUserInfoById(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void getUserInfoById_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.getUserInfoById(id));
        verify(userInfoRepository).findById(id);
    }

    @Test
    @DisplayName("getUserInfoById(): " + RETURNS_USER_INFO_WHEN_INFO_EXISTS)
    void getUserInfoById_UserInfoExists_ReturnsUserInfo() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.of(userInfo));

        UserInfo returnedUserInfo = userInfoService.getUserInfoById(id);
        assertNotNull(returnedUserInfo);
        verify(userInfoRepository).findById(id);
    }

    @Test
    @DisplayName("createUserInfo(): " + THROWS_USER_INFO_ALREADY_EXISTS_WHEN_INFO_ALREADY_EXISTS)
    void createUserInfo_UserInfoAlreadyExists_ThrowsUserInfoAlreadyExists() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.of(userInfo));

        assertThrows(UserInfoAlreadyExistsException.class, () -> userInfoService.createUserInfo(userCreationDTO, user));
        verify(userInfoRepository).findById(id);
        verify(userInfoRepository, never()).persist(any(UserInfo.class));
    }

    @Test
    @DisplayName("createUserInfo(): " + CREATES_USER_INFO_WHEN_USER_INFO_DOES_NOT_EXIST)
    void createUserInfo_UserInfoDoesNotExist_CreatesUserInfo() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.empty());

        userInfoService.createUserInfo(userCreationDTO, user);

        verify(userInfoRepository).findById(id);
        verify(userInfoRepository).persist(userInfoCaptor.capture());
        UserInfo createdUserInfo = userInfoCaptor.getValue();
        assertEquals(user.getId(), createdUserInfo.getId());
        assertEquals(user, createdUserInfo.getUser());
        assertEquals(userCreationDTO.firstName(), createdUserInfo.getFirstName());
        assertEquals(userCreationDTO.lastName(), createdUserInfo.getLastName());
    }

    @Test
    @DisplayName("deleteUserInfo(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void deleteUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.deleteUserInfo(id));
        verify(userInfoRepository).findById(id);
        verify(userInfoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteUserInfo(): " + DELETES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void deleteUserInfo_UserInfoExists_DeletesUserInfo() {
        Long id = userInfo.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.of(userInfo));

        userInfoService.deleteUserInfo(id);
        verify(userInfoRepository).findById(id);
        verify(userInfoRepository).deleteById(id);
    }

    @Test
    @DisplayName("updateUserInfo(): " + THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST)
    void updateUserInfo_UserInfoDoesNotExist_ThrowsUserInfoNotFoundException() {
        Long id = user.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserInfoNotFoundException.class, () -> userInfoService.updateUserInfo(id, userInfoDTO));
        verify(userInfoRepository).findById(id);
        verify(userInfoRepository, never()).update(any(UserInfo.class));
    }

    @Test
    @DisplayName("updateUserInfo(): " + UPDATES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void updateUserInfo_UserInfoExists_UpdatesUserInfo() {
        Long id = user.getId();
        given(userInfoRepository.findById(id)).willReturn(Optional.of(userInfo));

        UserInfo updatedUserInfo = userInfoService.updateUserInfo(id, userInfoDTO);
        assertEquals(updatedUserInfo.getFirstName(), userInfoDTO.firstName());
        assertEquals(updatedUserInfo.getLastName(), userInfoDTO.lastName());
        assertEquals(updatedUserInfo.getAvatarUrl(), userInfoDTO.avatarUrl());
        verify(userInfoRepository).findById(id);
        verify(userInfoRepository).update(userInfoCaptor.capture());
        UserInfo userInfoUpdated = userInfoCaptor.getValue();
        assertEquals(userInfoUpdated.getFirstName(), userInfoDTO.firstName());
        assertEquals(userInfoUpdated.getLastName(), userInfoDTO.lastName());
        assertEquals(userInfoUpdated.getAvatarUrl(), userInfoDTO.avatarUrl());
    }

}
