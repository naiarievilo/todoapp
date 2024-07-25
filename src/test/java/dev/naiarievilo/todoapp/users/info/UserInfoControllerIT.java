package dev.naiarievilo.todoapp.users.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.ControllerIntegrationTests;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.info.dtos.UserInfoDTO;
import dev.naiarievilo.todoapp.users.info.exceptions.UserInfoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.BEARER_PREFIX;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInfoControllerIT extends ControllerIntegrationTests {

    @Autowired
    UserService userService;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    private UserCreationDTO userCreationDTO;
    private User user;
    private UserInfo userInfo;
    private UserInfoDTO userInfoDTO;
    private Exception exception;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        userInfoDTO = new UserInfoDTO(null, NEW_FIRST_NAME, NEW_LAST_NAME, NEW_AVATAR_URL);
    }

    @Test
    @DisplayName("getUserInfo(): " + UserInfoControllerTestCases.STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_INFO_NOT_FOUND)
    void getUserInfo_UserInfoDoesNotExist_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        userInfoService.deleteUserInfo(user.getId());
        exception = new UserInfoNotFoundException(user.getId());

        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/info")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isNotFound(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);

        assertEquals(HttpStatus.NOT_FOUND.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("getUserInfo(): " + UserInfoControllerTestCases.STATUS_200_RETURNS_USER_INFO_DTO_WHEN_USER_INFO_EXISTS)
    void getUserInfo_UserInfoExists_ReturnsUserInfoDTO() throws Exception {
        user = userService.createUser(userCreationDTO);
        userInfo = userInfoService.getUserInfoById(user.getId());
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());

        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/info")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        userInfoDTO = objectMapper.readValue(responseBody, UserInfoDTO.class);
        assertEquals(user.getEmail(), userInfoDTO.email());
        assertEquals(userInfo.getFirstName(), userInfoDTO.firstName());
        assertEquals(userInfo.getLastName(), userInfoDTO.lastName());
        assertEquals(userInfo.getAvatarUrl(), userInfoDTO.avatarUrl());
    }

    @Test
    @DisplayName("updateUserInfo(): " + UserInfoControllerTestCases.STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_INFO_NOT_FOUND)
    void updateUserInfo_UserInfoDoesNotExist_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        userInfoService.deleteUserInfo(user.getId());
        exception = new UserInfoNotFoundException(user.getId());

        String responseBody = mockMvc.perform(patch("/users/" + user.getId() + "/info")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInfoDTO))
            )
            .andExpectAll(
                status().isNotFound(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);

        assertEquals(HttpStatus.NOT_FOUND.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("updateUserInfo(): " + UserInfoControllerTestCases.STATUS_200_UPDATES_USER_INFO_WHEN_USER_INFO_EXISTS)
    void updateUserInfo_UserInfoExists_UpdatesUserInfo() throws Exception {
        user = userService.createUser(userCreationDTO);
        userInfo = userInfoService.getUserInfoById(user.getId());
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());

        mockMvc.perform(patch("/users/" + user.getId() + "/info")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(userInfoDTO))
            )
            .andExpect(status().isOk());

        UserInfo updatedUserInfo = userInfoService.getUserInfoById(user.getId());
        assertEquals(userInfoDTO.firstName(), updatedUserInfo.getFirstName());
        assertEquals(userInfoDTO.lastName(), updatedUserInfo.getLastName());
        assertEquals(userInfoDTO.avatarUrl(), updatedUserInfo.getAvatarUrl());
    }
}
