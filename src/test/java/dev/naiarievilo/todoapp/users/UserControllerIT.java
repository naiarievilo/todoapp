package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.ControllerIntegrationTests;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.security.jwt.TokensDTO;
import dev.naiarievilo.todoapp.users.dtos.CredentialsUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.validation.ValidationMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationProvider.BAD_CREDENTIALS;
import static dev.naiarievilo.todoapp.security.jwt.JwtService.NEW_ACCESS_TOKEN_CREATION_FAILED;
import static dev.naiarievilo.todoapp.security.jwt.JwtService.TYPE_CLAIM;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.users.UserController.REFRESH_TOKEN_HEADER;
import static dev.naiarievilo.todoapp.users.UserControllerTestCases.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends ControllerIntegrationTests {

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
    @Autowired
    Environment environment;
    @Value("${spring.application.name}")
    String jwtIssuer;
    @Value("${jwt.secret}")
    String jwtSecret;
    private Algorithm jwtAlgorithm;
    private UserCreationDTO userCreationDTO;
    private UserCreationDTO otherUserCreationDTO;
    private UserDTO userDTO;
    private Exception exception;
    private User user;
    private JWTCreator.Builder expiredAccessTokenBuilder;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        otherUserCreationDTO = new UserCreationDTO(EMAIL_2, PASSWORD_2, CONFIRM_PASSWORD_2, FIRST_NAME_2, LAST_NAME_2);
        userDTO = new UserDTO(null, userCreationDTO.email(), userCreationDTO.password(), false);

        jwtAlgorithm = Algorithm.HMAC256(jwtSecret);
        Instant now = Instant.now();
        expiredAccessTokenBuilder = JWT.create()
            .withIssuer(jwtIssuer)
            .withIssuedAt(now.minusMillis(ACCESS_TOKEN.expirationInMillis()))
            .withClaim(TYPE_CLAIM, ACCESS_TOKEN.type())
            .withExpiresAt(now);
    }

    @Test
    @DisplayName("createUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void createUser_UserCreationDTONotValid_ReturnsErrorDetails() throws Exception {
        var invalidUserCreationDTO =
            new UserCreationDTO("notAValidEmail", PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

        String responseBody = mockMvc.perform(post("/users")
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
    @DisplayName("createUser(): " + STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoesNotExist_CreatesUser() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/users")
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
    @DisplayName("createUser(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        exception = new UserAlreadyExistsException(userCreationDTO.email());

        String responseBody = mockMvc.perform(post("/users")
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
        UserDTO invalidUserDTO = new UserDTO(null, EMAIL_1, " ", false);

        String responseBody = mockMvc.perform(post("/users/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserDTO.toString())
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
    @DisplayName("verifyUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_CREDENTIALS_INCORRECT)
    void authenticateUser_EmailOrPasswordIncorrect_ReturnsErrorDetails() throws Exception {
        userService.createUser(userCreationDTO);
        UserDTO userDtoWithWrongPassword = new UserDTO(null, EMAIL_1, "wrongSecurePassword123!?", false);

        String content = mockMvc.perform(post("/users/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDtoWithWrongPassword.toString())
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
    @DisplayName("verifyUser(): " + STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_CORRECT)
    void authenticateUser_CredentialsValid_AuthenticatesUser() throws Exception {
        userService.createUser(userCreationDTO);

        mockMvc
            .perform(post("/users/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDTO.toString())
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
        user = userService.createUser(userCreationDTO);

        String expiredAccessToken = expiredAccessTokenBuilder
            .withSubject(user.getId().toString())
            .sign(jwtAlgorithm);

        TokensDTO requiredTokens = new TokensDTO(expiredAccessToken, "invalidRefreshToken");

        String responseBody = mockMvc.perform(post("/users/" + user.getId() + "/re-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requiredTokens))
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
    @DisplayName("getNewAccessToken() " + STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_ACCESS_TOKEN_INVALID_BESIDES_EXPIRED)
    void getNewAccessToken_AccessTokenInvalidBesidesExpired_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String refreshToken = jwtService.createToken(user, REFRESH_TOKEN);

        Instant now = Instant.now();
        String invalidAccessToken = JWT.create()
            .withIssuedAt(now.minusMillis(ACCESS_TOKEN.expirationInMillis()))
            .withExpiresAt(now)
            .sign(jwtAlgorithm);

        TokensDTO requiredTokens = new TokensDTO(invalidAccessToken, refreshToken);

        String responseBody = mockMvc.perform(post("/users/" + user.getId() + "/re-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requiredTokens))
            )
            .andExpectAll(
                status().isUnauthorized(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED));
    }

    @Test
    @DisplayName("getNewAccessToken() " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_ACCESS_TOKEN_STILL_VALID)
    void getNewAccessToken_AccessTokenNotExpired_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String accessToken = tokens.get(ACCESS_TOKEN.key());
        String refreshToken = tokens.get(REFRESH_TOKEN.key());

        TokensDTO requiredTokens = new TokensDTO(accessToken, refreshToken);

        String responseBody = mockMvc.perform(post("/users/" + user.getId() + "/re-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requiredTokens))
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
        assertTrue(errorDetails.getMessages().contains(NEW_ACCESS_TOKEN_CREATION_FAILED));
    }

    @Test
    @DisplayName("getNewAccessToken(): " +
        STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_ACCESS_TOKEN_EXPIRED_AND_REFRESH_TOKEN_VALID)
    void getNewAccessToken_RefreshTokenValid_ReturnsNewAccessToken() throws Exception {
        user = userService.createUser(userCreationDTO);
        String refreshToken = jwtService.createToken(user, REFRESH_TOKEN);

        String expiredAccessToken = expiredAccessTokenBuilder
            .withSubject(user.getId().toString())
            .sign(jwtAlgorithm);

        TokensDTO requiredTokens = new TokensDTO(expiredAccessToken, refreshToken);

        String accessToken = mockMvc.perform(post("/users/" + user.getId() + "/re-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requiredTokens))
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
    @DisplayName("deleteUser(): " + STATUS_204_DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);

        mockMvc
            .perform(delete("/users/" + user.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteUser(): " + STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND)
    void deleteUser_UserDoesNotExist_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = BEARER_PREFIX + jwtService.createToken(user, ACCESS_TOKEN);
        exception = new UserNotFoundException(user.getId());

        mockMvc
            .perform(delete("/users/" + user.getId())
                .header(HttpHeaders.AUTHORIZATION, accessToken)
            )
            .andExpect(status().isNoContent());

        String responseBody = mockMvc.perform(delete("/users/" + user.getId())
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
    @DisplayName("updateEmail(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updateEmail_NewEmailNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO = new CredentialsUpdateDTO("invalidEmail", null, null, null);

        String responseBody = mockMvc.perform(patch("/users/" + user.getId() + "/email")
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
    @DisplayName("updateEmail(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        userService.createUser(otherUserCreationDTO);

        exception = new EmailAlreadyRegisteredException(otherUserCreationDTO.email());
        var updateCredentialsDTO = new CredentialsUpdateDTO(otherUserCreationDTO.email(), null, null, null);

        String responseBody = mockMvc.perform(patch("/users/" + user.getId() + "/email")
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
    @DisplayName("updateEmail(): " + STATUS_204_UPDATES_EMAIL_WHEN_NEW_EMAIL_VALID_AND_NOT_REGISTERED)
    void updateEmail_NewEmailValidAndNotRegistered_ReturnsNewAccessAndRefreshTokens() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO = new CredentialsUpdateDTO(NEW_EMAIL, null, null, null);

        mockMvc
            .perform(patch("/users/" + user.getId() + "/email")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("updatePassword(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updatePassword_NewPasswordNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO = new CredentialsUpdateDTO(
            userCreationDTO.email(), "", NEW_PASSWORD, "confirmNewPasswordDoesNotMatch123!"
        );

        List<String> expectedErrorMessages = List.of(
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "currentPassword"),
            ValidationMessages.formatMessage(DOES_NOT_MATCH, "newPasswordConfirmation", "newPassword")
        );

        String responseBody = mockMvc.perform(patch("/users/" + user.getId() + "/password")
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
    @DisplayName("updatePassword(): " + STATUS_204_UPDATES_PASSWORD_WHEN_NEW_PASSWORD_VALID)
    void updatePassword_NewPasswordValid_UpdatesPassword() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO = new CredentialsUpdateDTO(
            "", userCreationDTO.password(), NEW_PASSWORD, NEW_PASSWORD
        );

        mockMvc.perform(patch("/users/" + user.getId() + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("updateCredentials(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID)
    void updateCredentials_NewCredentialsNotValid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO = new CredentialsUpdateDTO("", "", "", "confirmPasswordDoesNotMatch");

        List<String> validationErrorMessages = List.of(
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "newEmail"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "currentPassword"),
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "newPassword"),
            ValidationMessages.formatMessage(DOES_NOT_MATCH, "newPasswordConfirmation", "newPassword")
        );

        String responseBody = mockMvc.perform(patch("/users/" + user.getId() + "/credentials")
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
    @DisplayName("updateCredentials(): " + STATUS_204_UPDATES_CREDENTIALS_WHEN_NEW_CREDENTIALS_VALID)
    void updateCredentials_NewCredentialsValid_UpdatesCredentials() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);
        var updateCredentialsDTO =
            new CredentialsUpdateDTO(NEW_EMAIL, userCreationDTO.password(), NEW_PASSWORD, NEW_PASSWORD);

        mockMvc.perform(patch("/users/" + user.getId() + "/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .content(objectMapper.writeValueAsString(updateCredentialsDTO))
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("verifyEmailRequest() : " + STATUS_200_SENDS_EMAIL_VERIFICATION_MESSAGE_WHEN_EMAIL_VALID)
    void verifyEmailRequest_EmailValid_SendsEmailVerificationMessage() throws Exception {
        user = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createToken(user, ACCESS_TOKEN);

        mockMvc.perform(post("/users/" + user.getId() + "/verification")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("verifyEmail(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_TOKEN_INVALID)
    void verifyEmail_TokenInvalid_ReturnsErrorDetails() throws Exception {
        user = userService.createUser(userCreationDTO);

        List<String> expectedErrorMessages = List.of(
            ValidationMessages.formatMessage(MUST_BE_PROVIDED, "emailVerificationToken")
        );

        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/verification?token=")
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().containsAll(expectedErrorMessages));
    }

    @Test
    @DisplayName("verifyEmail(): " + STATUS_200_VERIFIES_USER_EMAIL_WHEN_VERIFICATION_TOKEN_VALID)
    void verifyEmail_TokenValid_VerifiesUserEmail() throws Exception {
        user = userService.createUser(userCreationDTO);
        String emailConfirmationToken = jwtService.createToken(user, VERIFICATION_TOKEN);

        mockMvc.perform(get("/users/" + user.getId() + "/verification?token=" + emailConfirmationToken))
            .andExpect(status().isOk());

        user = userService.getUserById(user.getId());
        assertTrue(user.isVerified());
    }

    @Test
    @DisplayName("unlockUserRequest(): " + STATUS_200_SENDS_UNLOCK_USER_MESSAGE_WHEN_EMAIL_VALID)
    void unlockUserRequest_EmailValid_SendsUnlockUserMessage() throws Exception {
        user = userService.createUser(userCreationDTO);
        userService.lockUser(user);
        userDTO = new UserDTO(null, user.getEmail(), null, false);

        mockMvc.perform(post("/users/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("unlockUser(): " + STATUS_200_UNLOCKS_USER_WHEN_TOKEN_VALID)
    void unlockUser_UnlockTokenValid_UnlocksUser() throws Exception {
        user = userService.createUser(userCreationDTO);
        userService.lockUser(user);
        String unlockUserToken = jwtService.createToken(user, UNLOCK_TOKEN);

        mockMvc.perform(get("/users/" + user.getId() + "/unlock?token=" + unlockUserToken))
            .andExpect(status().isOk());

        user = userService.getUserById(user.getId());
        assertFalse(user.isLocked());
    }

    @Test
    @DisplayName("enableUserRequest(): " + STATUS_200_SENDS_ENABLE_USER_MESSAGE_WHEN_EMAIL_VALID)
    void enableUserRequest_EmailValid_SendsEnableUserMessage() throws Exception {
        user = userService.createUser(userCreationDTO);
        userService.disableUser(user);
        userDTO = new UserDTO(null, user.getEmail(), null, false);

        mockMvc.perform(post("/users/enable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO))
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("enableUser(): " + STATUS_200_ENABLES_USER_WHEN_ENABLE_TOKEN_VALID)
    void enableUser_EnableTokenValid_EnablesUser() throws Exception {
        user = userService.createUser(userCreationDTO);
        userService.disableUser(user);
        String enableUserToken = jwtService.createToken(user, ENABLE_TOKEN);

        mockMvc.perform(get("/users/" + user.getId() + "/enable?token=" + enableUserToken))
            .andExpect(status().isOk());

        user = userService.getUserById(user.getId());
        assertTrue(user.isEnabled());
    }
}