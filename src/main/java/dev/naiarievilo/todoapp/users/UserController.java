package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.mailing.EmailService;
import dev.naiarievilo.todoapp.security.AuthenticatedUser;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.UserAuthenticationToken;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.security.jwt.TokensDTO;
import dev.naiarievilo.todoapp.users.dtos.CredentialsUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.dtos.groups.*;
import dev.naiarievilo.todoapp.users.info.UserInfo;
import dev.naiarievilo.todoapp.users.info.UserInfoService;
import dev.naiarievilo.todoapp.users.info.dtos.UserInfoDTO;
import dev.naiarievilo.todoapp.validation.NotBlank;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.*;

@Tag(name = "User API")
@RestController
@RequestMapping("/users")
public class UserController {

    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserInfoService userInfoService;

    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService
        , EmailService emailService, UserInfoService userInfoService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.userInfoService = userInfoService;
    }

    @Operation(
        summary = "Create user",
        description = "Creates a user. All input is is validated: email provided must be a valid email; passwords " +
            "must contain between 14-72 characters and have at least one lower and upper case letter, one number, and" +
            " one special character; password confirmation must match and; first and last name should not be blank. " +
            "If successful, returns relevant user information and access and refresh tokens to be managed by the " +
            "client.",
        responses = {
            @ApiResponse(responseCode = "201",
                headers = {
                    @Header(name = "AUTHORIZATION", description = "Access token"),
                    @Header(name = "REFRESH-TOKEN", description = "Refresh token")
                },
                content = @Content(schema = @Schema(implementation = UserDTO.class), examples = {@ExampleObject(
                    value = "{\"id\": 1, \"verified\": \"false\"}"
                )})
            )
        }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        User user = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN.key()))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN.key()))
            .body(new UserDTO(user.getId(), null, null, user.isVerified()));
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a registered user. User has up to 10 authentication attempts before the " +
            "account is locked for the user's security. If successful, returns relevant user information and access " +
            "and refresh tokens to be managed by the client.",
        responses = {
            @ApiResponse(responseCode = "200",
                headers = {
                    @Header(name = "AUTHORIZATION", description = "Access token"),
                    @Header(name = "REFRESH-TOKEN", description = "Refresh token")
                },
                content = @Content(schema = @Schema(implementation = UserDTO.class), examples = {@ExampleObject(
                    value = "{\"id\": 1, \"verified\": \"false\"}"
                )})
            )
        }
    )
    @PostMapping("/authentication")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDTO> authenticateUser(@RequestBody @Validated(UserAuthentication.class) UserDTO userDTO) {
        var authentication = (UserAuthenticationToken) authenticationManager.authenticate(
            EmailPasswordAuthenticationToken.unauthenticated(userDTO.getEmail(), userDTO.getPassword())
        );

        User user = authentication.getPrincipal();
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN.key()))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN.key()))
            .body(new UserDTO(user.getId(), null, null, user.isVerified()));
    }

    @Operation(
        summary = "Verify user email",
        description = "Verifies a user account's email. Accepts a token parameter containing an email verification " +
            "token provided by the API. This endpoint is not intended to be used directly."
    )
    @GetMapping("/{userId}/verification")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@PathVariable Long userId, @RequestParam("token") @NotBlank String emailVerificationToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(emailVerificationToken, USER_VERIFICATION);
        Long tokenUserId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(tokenUserId);
        userService.verifyUser(user);
    }

    @Operation(
        summary = "Unlock user",
        description = "Unlocks a user account. Accepts a token parameter containing a user-unlocking token provided " +
            "by the API. This endpoint is not intended to be used directly."
    )
    @GetMapping("/{userId}/unlock")
    @ResponseStatus(HttpStatus.OK)
    public void unlockUser(@PathVariable Long userId, @RequestParam("token") @NotBlank String unlockToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(unlockToken, USER_UNLOCKING);
        Long tokenUserId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(tokenUserId);
        userService.unlockUser(user);
    }

    @Operation(
        summary = "Request to unlock user",
        description = "Sends a message to the email provided with instructions on how to unlock the user's account."
    )
    @PostMapping("/unlock")
    @ResponseStatus(HttpStatus.OK)
    public void unlockUserRequest(
        @RequestBody @Validated(UserSecurity.class) UserDTO userDTO
    ) {
        User user = userService.getUserByEmail(userDTO.getEmail());
        if (user.isLocked()) {
            emailService.sendUnlockUserMessage(user);
        }
    }

    @Operation(
        summary = "Enable user",
        description = "Enables a user account. Accepts a token parameter containing a user-enabling token provided " +
            "by the API. This endpoint is not intended to be used directly."
    )
    @GetMapping("/{userId}/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableUser(@PathVariable Long userId, @RequestParam("token") @NotBlank String enableToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(enableToken, USER_ENABLING);
        Long tokenUserId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(tokenUserId);
        userService.enableUser(user);
    }

    @Operation(
        summary = "Request to enable user",
        description = "Sends a message to the email provided with instructions on how to enable the user's account."
    )
    @PostMapping("/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableUserRequest(@RequestBody @Validated(UserSecurity.class) UserDTO userDTO) {
        User user = userService.getUserByEmail(userDTO.getEmail());
        if (!user.isEnabled()) {
            emailService.sendEnableUserMessage(user);
        }
    }

    @Operation(
        summary = "Re-authenticate user",
        description = "Re-authenticates a user. Accepts a valid refresh token and an <b>expired</b> access token. " +
            "Returns a new access token if request is successful",
        responses = {
            @ApiResponse(responseCode = "200", headers = {
                @Header(name = "AUTHORIZATION", description = "New access token")
            })
        }
    )
    @PostMapping("/{userId}/re-authentication")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> getNewAccessToken(@PathVariable Long userId, @RequestBody TokensDTO tokensDTO) {
        String newAccessToken = jwtService.createAccessToken(tokensDTO.accessToken(), tokensDTO.refreshToken());
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            .build();
    }

    @Operation(
        summary = "Delete user",
        security = {@SecurityRequirement(name = "Access Token")})
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @Operation(
        summary = "Update email",
        security = {@SecurityRequirement(name = "Access Token")},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                schema = @Schema(implementation = CredentialsUpdateDTO.class),
                examples = {@ExampleObject(value = "{\"new_email\": \"string\"}")}
            ))
    )
    @PatchMapping("/{userId}/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateEmail(
        @PathVariable Long userId,
        @AuthenticatedUser User user,
        @RequestBody @Validated(EmailUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
    }

    @Operation(
        summary = "Request to verify email",
        description = "Sends an email verification message to the user's email containing instructions on how to " +
            "verify the user's account. A user has one week to verify their email's account after its registration, " +
            "otherwise the account is deleted automatically.",
        security = {@SecurityRequirement(name = "Access Token")})
    @PostMapping("/{userId}/verification")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmailRequest(@PathVariable Long userId, @AuthenticatedUser User user) throws MailException {
        emailService.sendEmailVerificationMessage(user);
    }

    @Operation(
        summary = "Update password",
        security = {@SecurityRequirement(name = "Access Token")},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                schema = @Schema(implementation = CredentialsUpdateDTO.class),
                examples = {@ExampleObject(value = "{\"current_password\": \"string\", \"new_password\": \"string\", " +
                    "\"confirm_new_password\": \"string\"}"
                )}
            ))
    )
    @PatchMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
        @PathVariable Long userId,
        @AuthenticatedUser User user,
        @RequestBody @Validated(PasswordUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

    @Operation(summary = "Update email and password", security = {@SecurityRequirement(name = "Access Token")})
    @PatchMapping("/{userId}/credentials")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCredentials(
        @PathVariable Long userId,
        @AuthenticatedUser User user,
        @RequestBody @Validated(CredentialsUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

    @Operation(summary = "Get user information", security = {@SecurityRequirement(name = "Access Token")})
    @GetMapping("{userId}/info")
    @ResponseStatus(HttpStatus.OK)
    public UserInfoDTO getUserInfo(@PathVariable Long userId, @AuthenticatedUser User user) {
        UserInfo userInfo = userInfoService.getUserInfoById(userId);
        return new UserInfoDTO(user.getEmail(), userInfo.getFirstName(), userInfo.getLastName(),
            userInfo.getAvatarUrl());
    }

    @Operation(
        summary = "Update user information",
        security = {@SecurityRequirement(name = "Access Token")},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                schema = @Schema(implementation = UserInfoDTO.class),
                examples = {@ExampleObject(value = "{\"first_name\": \"string\", \"last_name\": \"string\", " +
                    "\"avatar_url\": \"string\"}")}
            ))
    )
    @PatchMapping("{userId}/info")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserInfo(@PathVariable Long userId, @RequestBody @Valid UserInfoDTO userInfoDTO) {
        userInfoService.updateUserInfo(userId, userInfoDTO);
    }
}
