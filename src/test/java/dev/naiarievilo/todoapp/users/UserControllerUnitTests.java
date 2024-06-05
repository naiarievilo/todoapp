package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationProvider.BAD_CREDENTIALS;
import static dev.naiarievilo.todoapp.security.JwtConstants.*;
import static dev.naiarievilo.todoapp.users.UserController.REFRESH_TOKEN_HEADER;
import static dev.naiarievilo.todoapp.users.UserControllerTestCaseMessages.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.EMAIL_MUST_BE_VALID;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.PASSWORD_MUST_BE_PROVIDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerUnitTests {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private final Map<String, String> tokens = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @MockBean
    UserService userService;
    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    JwtService jwtService;

    @Autowired
    MockMvc mockMvc;
    private UserCreationDTO userCreationDTO;
    private UserAuthenticationDTO userAuthenticationDTO;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);
        userAuthenticationDTO = new UserAuthenticationDTO(EMAIL, PASSWORD);

        userPrincipal = UserPrincipalImpl.builder()
            .setId(1L)
            .setEmail(EMAIL)
            .setPassword(PASSWORD)
            .setRoles(Roles.ROLE_USER)
            .setEnabled(true)
            .setLocked(false)
            .build();

        tokens.put(ACCESS_TOKEN, "validAccessToken");
        tokens.put(REFRESH_TOKEN, "validRefreshToken");
    }

    @Test
    @DisplayName("createUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_CREATION_DTO_NOT_VALID)
    void createUser_UserCreationDTOIsNotValid_ReturnsErrorMessage() throws Exception {
        UserCreationDTO InvalidUserCreationDTO =
            new UserCreationDTO("notAValidEmail", PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);

        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(InvalidUserCreationDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(EMAIL_MUST_BE_VALID)
            );

        verifyNoInteractions(userService);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("createUser(): " + STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ReturnsErrorMessage() throws Exception {
        var userAlreadyExistsException = new UserAlreadyExistsException(userCreationDTO.email());

        given(userService.createUser(userCreationDTO))
            .willThrow(userAlreadyExistsException);

        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(userCreationDTO))
            )
            .andExpectAll(
                status().isConflict(),
                content().string(userAlreadyExistsException.getMessage())
            );

        verify(userService).createUser(userCreationDTO);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("createUser(): " + STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoesNotExist_CreatesUser() throws Exception {

        given(userService.createUser(userCreationDTO)).willReturn(userPrincipal);
        given(jwtService.createAccessAndRefreshTokens(userPrincipal))
            .willReturn(tokens);

        mockMvc
            .perform(post("/users/create")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(userCreationDTO))
            )
            .andExpectAll(
                status().isCreated(),
                header().string(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN)),
                header().string(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            );

        verify(userService).createUser(userCreationDTO);
        verify(jwtService).createAccessAndRefreshTokens(userPrincipal);
    }

    @Test
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_AUTHENTICATION_DTO_NOT_VALID)
    void authenticateUser_UserAuthenticationDTOIsNotValid_ReturnsErrorMessage() throws Exception {
        UserAuthenticationDTO InvalidUserAuthenticationDTO = new UserAuthenticationDTO(EMAIL, " ");

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(InvalidUserAuthenticationDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(PASSWORD_MUST_BE_PROVIDED)
            );

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("authenticateUser(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_EMAIL_OR_PASSWORD_INCORRECT)
    void authenticateUser_EmailOrPasswordIncorrect_ReturnsErrorMessage() throws Exception {
        BadCredentialsException badCredentialsException = new BadCredentialsException(BAD_CREDENTIALS);

        given(authenticationManager.authenticate(any(EmailPasswordAuthenticationToken.class)))
            .willThrow(badCredentialsException);

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(userAuthenticationDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().string(badCredentialsException.getMessage())
            );

        verify(authenticationManager).authenticate(any(EmailPasswordAuthenticationToken.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("authenticateUser(): " + STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_ARE_VALID)
    void authenticateUser_CredentialsAreValid_AuthenticatesUser() throws Exception {
        given(userService.loadUserByEmail(userAuthenticationDTO.email())).willReturn(userPrincipal);
        given(jwtService.createAccessAndRefreshTokens(userPrincipal)).willReturn(tokens);

        mockMvc
            .perform(post("/users/authenticate")
                .contentType(DEFAULT_CONTENT_TYPE)
                .content(objectMapper.writeValueAsString(userAuthenticationDTO))
            )
            .andExpectAll(
                status().isOk(),
                header().string(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN)),
                header().string(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            );

        verify(authenticationManager).authenticate(any(EmailPasswordAuthenticationToken.class));
        verify(userService).loadUserByEmail(userAuthenticationDTO.email());
        verify(jwtService).createAccessAndRefreshTokens(userPrincipal);
    }

    @Test
    @DisplayName("getNewAccessToken(): " + STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_REFRESH_TOKEN_NOT_VALID)
    void getNewAccessToken_RefreshTokenIsNotValid_ReturnsErrorMessage() throws Exception {
        String refreshToken = tokens.get(REFRESH_TOKEN);
        JWTVerificationException jwtVerificationException = new JWTVerificationException("JWT is not valid");
        given(jwtService.createAccessToken(refreshToken)).willThrow(jwtVerificationException);

        mockMvc
            .perform(put("/users/re-authenticate")
                .header(HttpHeaders.AUTHORIZATION, refreshToken)
            )
            .andExpectAll(
                status().isUnauthorized(),
                content().string(jwtVerificationException.getMessage())
            );

        verify(jwtService).createAccessToken(refreshToken);
    }

    @Test
    @DisplayName("getNewAccessToken(): " + STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_REFRESH_TOKEN_VALID)
    void getNewAccessToken_RefreshTokenIsValid_ReturnsNewAccessToken() throws Exception {
        String refreshToken = tokens.get(REFRESH_TOKEN);
        String newAccessToken = "newValidAccessToken";
        given(jwtService.createAccessToken(refreshToken)).willReturn(newAccessToken);

        mockMvc
            .perform(put("/users/re-authenticate")
                .header(HttpHeaders.AUTHORIZATION, refreshToken)
            )
            .andExpectAll(
                status().isOk(),
                header().string(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            );

        verify(jwtService).createAccessToken(refreshToken);
    }

    @Test
    @DisplayName("deleteUser(): " + STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND)
    void deleteUser_UserDoesNotExist_ReturnsErrorMessage() throws Exception {
        SecurityContextHolder.setContext(securityContext);
        String email = userPrincipal.getEmail();
        var userNotFoundException = new UserNotFoundException(email);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(email);
        doThrow(userNotFoundException).when(userService).deleteUser(email);

        mockMvc
            .perform(delete("/users/delete")
                .header(HttpHeaders.AUTHORIZATION, tokens.get(ACCESS_TOKEN))
            )
            .andExpect(status().isNotFound());

        verify(authentication).getPrincipal();
        verify(userService).deleteUser(email);
    }

    @Test
    @DisplayName("deleteUser() : " + STATUS_204_DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() throws Exception {
        SecurityContextHolder.setContext(securityContext);
        String email = userPrincipal.getEmail();

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(email);
        doNothing().when(userService).deleteUser(email);

        mockMvc
            .perform(delete("/users/delete")
                .header(HttpHeaders.AUTHORIZATION, tokens.get(ACCESS_TOKEN))
            )
            .andExpect(status().isNoContent());

        verify(authentication).getPrincipal();
        verify(userService).deleteUser(email);
    }
}
