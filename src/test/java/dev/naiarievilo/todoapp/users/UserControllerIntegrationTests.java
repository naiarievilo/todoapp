package dev.naiarievilo.todoapp.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.dtos.CredentialsUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.dtos.UserRolesUpdateDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.validation.ValidationMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationProvider.BAD_CREDENTIALS;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.users.UserController.REFRESH_TOKEN_HEADER;
import static dev.naiarievilo.todoapp.users.UserControllerTestCaseMessages.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class UserControllerIntegrationTests {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    JwtService jwtService;
    @Autowired
    MockMvc mockMvc;
    @Value("${ADMIN_EMAIL}")
    private String adminEmail;
    private UserCreationDTO userCreationDTO;
    private UserCreationDTO otherUserCreationDTO;
    private UserDTO userDTO;
    private Exception exception;
    private User user;

    UserControllerIntegrationTests() throws Exception {
    }

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        otherUserCreationDTO = new UserCreationDTO(EMAIL_2, PASSWORD_2, CONFIRM_PASSWORD_2, FIRST_NAME_2, LAST_NAME_2);
        userDTO = new UserDTO(null, userCreationDTO.email(), userCreationDTO.password());
    }

    @Test
    @DisplayName("createUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void createUser_UserCreationDTONotValid_ReturnsErrorDetails() throws Exception {
        var invalidUserCreationDTO =
            new UserCreationDTO("notAValidEmail", PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

        String responseBody = mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserCreationDTO))
            )
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        String expectedErrorMessage = ValidationMessages.formatMessage(NOT_VALID, "email");
        assertTrue(errorDetails.getMessages().contains(expectedErrorMessage));
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoesNotExist_CreatesUser() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreationDTO))
            )
            .andExpectAll(
                status().isCreated(),
                header().exists(HttpHeaders.AUTHORIZATION),
                header().exists(REFRESH_TOKEN_HEADER)
            )
            .andReturn().getResponse();

        String accessToken = response.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken = response.getHeader(REFRESH_TOKEN_HEADER);
        assertNotNull(refreshToken);
        assertNotNull(accessToken);
        assertTrue(accessToken.startsWith(BEARER_PREFIX));
        assertTrue(refreshToken.startsWith(BEARER_PREFIX));
        assertTrue(accessToken.replaceFirst(BEARER_PREFIX, "").matches(JWT_REGEX));
        assertTrue(refreshToken.replaceFirst(BEARER_PREFIX, "").matches(JWT_REGEX));
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        exception = new UserAlreadyExistsException(userCreationDTO.email());

        String responseBody = mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreationDTO))
            )
            .andExpect(status().isConflict())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.CONFLICT.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void authenticateUser_UserAuthenticationDTONotValid_ReturnsErrorDetails() throws Exception {
        UserDTO invalidUserDTO = new UserDTO(null, EMAIL_1, " ");

        String responseBody = mockMvc.perform(post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDTO))
            )
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        String expectedErrorMessage = ValidationMessages.formatMessage(MUST_BE_PROVIDED, "password");
        assertTrue(errorDetails.getMessages().contains(expectedErrorMessage));
    }

    @Test
    @Transactional
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_CREDENTIALS_INCORRECT)
    void authenticateUser_EmailOrPasswordIncorrect_ReturnsErrorDetails() throws Exception {
        userService.createUser(userCreationDTO);
        UserDTO userDTOWithWrongPassword = new UserDTO(null, EMAIL_1, "wrongSecurePassword123!?");

        String content = mockMvc.perform(post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTOWithWrongPassword))
            )
            .andExpectAll(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(content, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(BAD_CREDENTIALS));
    }

    @Test
    @Transactional
    @DisplayName("authenticateUser(): " + STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_CORRECT)
    void authenticateUser_CredentialsValid_AuthenticatesUser() throws Exception {
        userService.createUser(userCreationDTO);

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO))
            )
            .andExpectAll(
                status().isOk(),
                header().exists(HttpHeaders.AUTHORIZATION),
                header().exists(REFRESH_TOKEN_HEADER)
            );
    }

    @Test
    @DisplayName("getNewAccessToken(): " + STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_REFRESH_TOKEN_NOT_VALID)
    void getNewAccessToken_RefreshTokenNotValid_ReturnsErrorDetails() throws Exception {
        String responseBody = mockMvc.perform(post("/users/current/reauthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "invalidToken")
            )
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED));
    }

    @Test
    @Transactional
    @DisplayName("getNewAccessToken(): " + STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_REFRESH_TOKEN_VALID)
    void getNewAccessToken_RefreshTokenValid_ReturnsNewAccessToken() throws Exception {
        user = userService.createUser(userCreationDTO);
        String refreshToken = jwtService.createAccessAndRefreshTokens(user).get(REFRESH_TOKEN.key());

        String accessToken = mockMvc.perform(post("/users/current/reauthenticate")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + refreshToken)
            )
            .andExpectAll(
                status().isOk(),
                header().exists(HttpHeaders.AUTHORIZATION)
            )
            .andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        assertNotNull(accessToken);
        assertTrue(accessToken.startsWith(BEARER_PREFIX));
        assertTrue(accessToken.replaceFirst(BEARER_PREFIX, "").matches(JWT_REGEX));
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + STATUS_200_DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());

        mockMvc
            .perform(delete("/users/current/delete")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND)
    void deleteUser_UserDoesNotExist_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = BEARER_PREFIX + jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        exception = new UserNotFoundException(user.getId());

        mockMvc
            .perform(delete("/users/current/delete")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
            )
            .andExpect(status().isOk());

        String responseBody = mockMvc.perform(delete("/users/current/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
            )
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updateEmail_NewEmailNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO = new CredentialsUpdateDTO("invalidEmail", null, null, null);

        String responseBody = mockMvc.perform(patch("/users/current/email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        String expectedErrorMessage = ValidationMessages.formatMessage(NOT_VALID, "newEmail");
        assertTrue(errorDetails.getMessages().contains(expectedErrorMessage));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        userService.createUser(otherUserCreationDTO);

        exception = new EmailAlreadyRegisteredException(otherUserCreationDTO.email());
        var updateCredentialsDTO = new CredentialsUpdateDTO(otherUserCreationDTO.email(), null, null, null);

        String responseBody = mockMvc.perform(patch("/users/current/email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isConflict(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.CONFLICT.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_200_UPDATES_EMAIL_WHEN_NEW_EMAIL_VALID_AND_NOT_REGISTERED)
    void updateEmail_NewEmailValidAndNotRegistered_ReturnsNewAccessAndRefreshTokens() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO = new CredentialsUpdateDTO(NEW_EMAIL, null, null, null);

        mockMvc
            .perform(patch("/users/current/email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updatePassword_NewPasswordNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO = new CredentialsUpdateDTO(
            userCreationDTO.email(), "", NEW_PASSWORD, "confirmNewPasswordDoesNotMatch123!"
        );

        List<String> expectedErrorMessages = List.of(
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "currentPassword"),
            ValidationMessages.formatMessage(DOES_NOT_MATCH, "newPasswordConfirmation", "newPassword")
        );

        String responseBody = mockMvc.perform(patch("/users/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().containsAll(expectedErrorMessages));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + STATUS_200_UPDATES_PASSWORD_WHEN_NEW_PASSWORD_VALID)
    void updatePassword_NewPasswordValid_UpdatesPassword() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO = new CredentialsUpdateDTO(
            "", userCreationDTO.password(), NEW_PASSWORD, NEW_PASSWORD
        );

        mockMvc.perform(patch("/users/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("updateCredentials(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updateCredentials_NewCredentialsNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO = new CredentialsUpdateDTO("", "", "", "confirmPasswordDoesNotMatch");

        List<String> validationErrorMessages = List.of(
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "newEmail"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "currentPassword"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "newPassword"),
            ValidationMessages.formatMessage(DOES_NOT_MATCH, "newPasswordConfirmation", "newPassword")
        );

        String responseBody = mockMvc.perform(patch("/users/current/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().containsAll(validationErrorMessages));
    }

    @Test
    @Transactional
    @DisplayName("updateCredentials(): " + STATUS_200_UPDATES_CREDENTIALS_WHEN_NEW_CREDENTIALS_VALID)
    void updateCredentials_NewCredentialsValid_UpdatesCredentials() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(user).get(ACCESS_TOKEN.key());
        var updateCredentialsDTO =
            new CredentialsUpdateDTO(NEW_EMAIL, userCreationDTO.password(), NEW_PASSWORD, NEW_PASSWORD);

        mockMvc.perform(patch("/users/current/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser(): " + STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN)
    void addRoleToUser_AuthenticatedUserNotAdmin_ReturnsForbidden() throws Exception {
        User newUser = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(newUser).get(ACCESS_TOKEN.key());
        var addAdminRoleToSelf = new UserRolesUpdateDTO(newUser.getId(), ROLE_ADMIN.name());

        mockMvc
            .perform(patch("/users/admin/assign-role")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addAdminRoleToSelf))
            )
            .andExpect(
                status().isForbidden()
            );
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void addRoleToUser_UserIdOrRoleNotValid_ReturnsErrorDetails() throws Exception {
        User admin = userService.getUserByEmail(adminEmail);
        String accessToken = jwtService.createAccessAndRefreshTokens(admin).get(ACCESS_TOKEN.key());
        var userRolesUpdate = new UserRolesUpdateDTO(-1L, "");

        List<String> expectedErrorMessages = List.of(
            ValidationMessages.formatMessage(NOT_VALID, "id"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "role")
        );

        String responseBody = mockMvc.perform(patch("/users/admin/assign-role")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(userRolesUpdate))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().containsAll(expectedErrorMessages));
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser(): " + STATUS_200_ADDS_ROLE_TO_USER_WHEN_AUTHENTICATED_USER_ADMIN)
    void addRoleToUser_AuthenticatedUserAdmin_AddsRoleToUser() throws Exception {
        User admin = userService.getUserByEmail(adminEmail);
        String accessToken = jwtService.createAccessAndRefreshTokens(admin).get(ACCESS_TOKEN.key());

        User newUser = userService.createUser(userCreationDTO);
        UserRolesUpdateDTO userRolesUpdateDTO = new UserRolesUpdateDTO(newUser.getId(), ROLE_ADMIN.name());

        mockMvc.perform(patch("/users/admin/assign-role")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(userRolesUpdateDTO))
            )
            .andExpect(status().isOk());

        User updatedUser = userService.getUserById(newUser.getId());
        Role adminRole = roleService.getRole(ROLE_ADMIN);
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN)
    void removeRoleFromUser_PrincipalNotAdmin_ReturnsForbidden() throws Exception {
        User newUser = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(newUser).get(ACCESS_TOKEN.key());

        User admin = userService.getUserByEmail(adminEmail);
        var userRolesUpdateDTO = new UserRolesUpdateDTO(admin.getId(), ROLE_ADMIN.name());

        mockMvc.perform(patch("/users/admin/unassign-role")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRolesUpdateDTO))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void removeRoleFromUser_UserIdOrRoleNotValid_ReturnsErrorDetails() throws Exception {
        User admin = userService.getUserByEmail(adminEmail);
        String accessToken = jwtService.createAccessAndRefreshTokens(admin).get(ACCESS_TOKEN.key());
        var userRolesUpdateDTO = new UserRolesUpdateDTO(-1L, "");

        List<String> expectedErrorMessages = List.of(
            ValidationMessages.formatMessage(NOT_VALID, "id"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "role")
        );

        String responseBody = mockMvc.perform(patch("/users/admin/unassign-role")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRolesUpdateDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().containsAll(expectedErrorMessages));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + STATUS_200_REMOVES_ROLE_FROM_USER_WHEN_AUTHENTICATED_USER_ADMIN)
    void removeRoleFromUser_AuthenticatedUserAdmin_RemovesRoleFromUser() throws Exception {
        User admin = userService.getUserByEmail(adminEmail);
        String accessToken = jwtService.createAccessAndRefreshTokens(admin).get(ACCESS_TOKEN.key());
        var userRolesUpdateDTO = new UserRolesUpdateDTO(admin.getId(), ROLE_ADMIN.name());

        mockMvc.perform(patch("/users/admin/unassign-role")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRolesUpdateDTO))
            )
            .andExpect(status().isOk());

        User updatedAdmin = userService.getUserByEmail(adminEmail);
        Role adminRole = roleService.getRole(ROLE_ADMIN);
        assertFalse(updatedAdmin.getRoles().contains(adminRole));
    }
}
