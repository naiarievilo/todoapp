package dev.naiarievilo.todoapp.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.users.dtos.UpdateCredentialsDTO;
import dev.naiarievilo.todoapp.users.dtos.UserAuthenticationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationProvider.BAD_CREDENTIALS;
import static dev.naiarievilo.todoapp.security.JwtConstants.*;
import static dev.naiarievilo.todoapp.users.UserController.REFRESH_TOKEN_HEADER;
import static dev.naiarievilo.todoapp.users.UserControllerTestCaseMessages.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.EMAIL_MUST_BE_VALID;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.PASSWORD_MUST_BE_PROVIDED;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional(readOnly = true)
class UserControllerIntegrationTests {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UserService userService;
    @Autowired
    JwtService jwtService;

    @Autowired
    MockMvc mockMvc;

    private UserCreationDTO userCreationDTO;
    private UserCreationDTO otherUserCreationDTO;
    private UserAuthenticationDTO userAuthenticationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        otherUserCreationDTO = new UserCreationDTO(EMAIL_2, PASSWORD_2, CONFIRM_PASSWORD_2, FIRST_NAME_2, LAST_NAME_2);
        userAuthenticationDTO = new UserAuthenticationDTO(userCreationDTO.email(), userCreationDTO.password());
    }

    @Test
    @DisplayName("createUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_CREATION_DTO_NOT_VALID)
    void createUser_UserCreationDTONotValid_ReturnsErrorMessage() throws Exception {
        UserCreationDTO invalidUserCreationDTO =
            new UserCreationDTO("notAValidEmail", PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(invalidUserCreationDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(EMAIL_MUST_BE_VALID)
            );
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoesNotExist_CreatesUser() throws Exception {
        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(userCreationDTO))
            )
            .andExpectAll(
                status().isCreated(),
                header().exists(HttpHeaders.AUTHORIZATION),
                header().exists(REFRESH_TOKEN_HEADER)
            );
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ReturnsErrorMessage() throws Exception {
        userService.createUser(userCreationDTO);
        var userAlreadyExistsException = new UserAlreadyExistsException(userCreationDTO.email());

        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(userCreationDTO))
            )
            .andExpectAll(
                status().isConflict(),
                content().string(userAlreadyExistsException.getMessage())
            );
    }

    @Test
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_AUTHENTICATION_DTO_NOT_VALID)
    void authenticateUser_UserAuthenticationDTONotValid_ReturnsErrorMessage() throws Exception {
        UserAuthenticationDTO InvalidUserAuthenticationDTO = new UserAuthenticationDTO(EMAIL_1, " ");

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(InvalidUserAuthenticationDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(PASSWORD_MUST_BE_PROVIDED)
            );
    }

    @Test
    @Transactional
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_EMAIL_OR_PASSWORD_INCORRECT)
    void authenticateUser_EmailOrPasswordIncorrect_ReturnsErrorMessage() throws Exception {
        userService.createUser(userCreationDTO);
        UserAuthenticationDTO withWrongPassword = new UserAuthenticationDTO(EMAIL_1, "wrongPassword");

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(withWrongPassword))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(BAD_CREDENTIALS)
            );
    }

    @Test
    @Transactional
    @DisplayName("authenticateUser(): " + STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_ARE_VALID)
    void authenticateUser_CredentialsValid_AuthenticatesUser() throws Exception {
        userService.createUser(userCreationDTO);

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(userAuthenticationDTO))
            )
            .andExpectAll(
                status().isOk(),
                header().exists(HttpHeaders.AUTHORIZATION),
                header().exists(REFRESH_TOKEN_HEADER)
            );
    }

    @Test
    @DisplayName("getNewAccessToken(): " + STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_REFRESH_TOKEN_NOT_VALID)
    void getNewAccessToken_RefreshTokenNotValid_ReturnsErrorMessage() throws Exception {
        mockMvc
            .perform(put("/users/reauthenticate")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "invalidToken")
            )
            .andExpectAll(
                status().isUnauthorized(),
                content().string(containsString(JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED))
            );
    }

    @Test
    @Transactional
    @DisplayName("getNewAccessToken(): " + STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_REFRESH_TOKEN_VALID)
    void getNewAccessToken_RefreshTokenValid_ReturnsNewAccessToken() throws Exception {
        Authentication authentication = userService.createUser(userCreationDTO);
        String refreshToken =
            BEARER_PREFIX + jwtService.createAccessAndRefreshTokens(authentication).get(REFRESH_TOKEN);

        mockMvc
            .perform(put("/users/reauthenticate")
                .header(HttpHeaders.AUTHORIZATION, refreshToken)
            )
            .andExpectAll(
                status().isOk(),
                header().exists(HttpHeaders.AUTHORIZATION)
            );
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + STATUS_204_DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() throws Exception {
        Authentication authentication = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(authentication).get(ACCESS_TOKEN);

        mockMvc
            .perform(delete("/users/delete")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND)
    void deleteUser_UserDoesNotExist_ReturnsErrorMessage() throws Exception {
        Authentication authentication = userService.createUser(userCreationDTO);
        String accessToken = BEARER_PREFIX + jwtService.createAccessAndRefreshTokens(authentication).get(ACCESS_TOKEN);
        var userNotFoundException = new UserNotFoundException(userCreationDTO.email());

        mockMvc
            .perform(delete("/users/delete")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
            )
            .andExpect(status().isNoContent());

        mockMvc
            .perform(delete("/users/delete")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
            )
            .andExpectAll(
                status().isNotFound(),
                content().string(containsString(userNotFoundException.getMessage()))
            );
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_NOT_VALID)
    void updateEmail_EmailNotValid_ReturnsErrorMessage() throws Exception {
        Authentication authentication = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(authentication).get(ACCESS_TOKEN);
        var updateCredentialsDTO = new UpdateCredentialsDTO("invalidEmail", "", "", "");

        mockMvc
            .perform(post("/users/update-email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(containsString(EMAIL_MUST_BE_VALID))
            );

    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_ALREADY_REGISTERED)
    void updateEmail_EmailAlreadyRegistered_ReturnsErrorMessage() throws Exception {
        userService.createUser(otherUserCreationDTO);
        Authentication authentication = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(authentication).get(ACCESS_TOKEN);
        var emailAlreadyRegisteredException = new EmailAlreadyRegisteredException(otherUserCreationDTO.email());
        var updateCredentialsDTO = new UpdateCredentialsDTO(otherUserCreationDTO.email(), "", "", "");

        mockMvc
            .perform(post("/users/update-email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isConflict(),
                content().string(emailAlreadyRegisteredException.getMessage())
            );
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + STATUS_200_UPDATES_USER_EMAIL_TO_NEW_EMAIL_WHEN_EMAIL_VALID_AND_NOT_REGISTERED)
    void updateEmail_EmailValidAndNotRegistered_ReturnsNewAccessAndRefreshTokens() throws Exception {
        Authentication authentication = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(authentication).get(ACCESS_TOKEN);
        var updateCredentialsDTO = new UpdateCredentialsDTO(NEW_EMAIL, "", "", "");

        mockMvc
            .perform(post("/users/update-email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(mapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpectAll(
                status().isOk(),
                header().exists(HttpHeaders.AUTHORIZATION),
                header().exists(REFRESH_TOKEN_HEADER)
            );
    }
}
