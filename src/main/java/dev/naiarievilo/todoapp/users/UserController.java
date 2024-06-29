package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.mailing.EmailService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.AuthenticatedUser;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.UserAuthenticationToken;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.dtos.CredentialsUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.dtos.UserRolesUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.groups.*;
import dev.naiarievilo.todoapp.validation.NotBlank;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.*;

@RestController
@RequestMapping("/users")
public class UserController {

    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserService userService;

    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService
        , EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/creation")
    public ResponseEntity<Void> createUser(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        User user = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN.key()))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN.key()))
            .build();
    }

    @PostMapping("/authentication")
    public ResponseEntity<UserDTO> authenticateUser(@RequestBody @Validated(UserAuthentication.class) UserDTO userDTO) {
        var authentication = (UserAuthenticationToken) authenticationManager.authenticate(
            EmailPasswordAuthenticationToken.unauthenticated(userDTO.email(), userDTO.password())
        );

        User user = authentication.getPrincipal();
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN.key()))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN.key()))
            .body(new UserDTO(user.getId(), user.getEmail(), user.getPassword()));
    }

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@RequestParam("token") @NotBlank String emailVerificationToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(emailVerificationToken, USER_VERIFICATION);
        Long userId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(userId);
        userService.authenticateUser(user);
    }

    @GetMapping("/unlock")
    @ResponseStatus(HttpStatus.OK)
    public void unlockUser(@RequestParam("token") @NotBlank String unlockToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(unlockToken, USER_UNLOCKING);
        Long userId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(userId);
        userService.unlockUser(user);
    }

    @PostMapping("/unlock")
    @ResponseStatus(HttpStatus.OK)
    public void unlockUserRequest(@RequestBody @Validated(UserSecurity.class) UserDTO userDTO) {
        User user = userService.getUserByEmail(userDTO.email());
        if (user.isLocked()) {
            emailService.sendUnlockUserMessage(user);
        }
    }

    @GetMapping("/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableUser(@RequestParam("token") @NotBlank String enableToken) {
        DecodedJWT verifiedJWT = jwtService.verifyToken(enableToken, USER_ENABLING);
        Long userId = Long.valueOf(verifiedJWT.getSubject());
        User user = userService.getUserById(userId);
        userService.enableUser(user);
    }

    @PostMapping("/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableUserRequest(@RequestBody @Validated(UserSecurity.class) UserDTO userDTO) {
        User user = userService.getUserByEmail(userDTO.email());
        if (!user.isEnabled()) {
            emailService.sendEnableUserMessage(user);
        }
    }

    @PostMapping("/current/re-authentication")
    public ResponseEntity<Void> getNewAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
        String newAccessToken = jwtService.createAccessToken(refreshToken);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            .build();
    }

    @DeleteMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@AuthenticatedUser User user) {
        userService.deleteUser(user.getId());
    }

    @PatchMapping("/current/email")
    @ResponseStatus(HttpStatus.OK)
    public void updateEmail(
        @AuthenticatedUser User user,
        @RequestBody @Validated(EmailUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
    }

    @PostMapping("/current/email/verification")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmailRequest(@AuthenticatedUser User user) throws MailException {
        emailService.sendEmailVerificationMessage(user);
    }

    @PatchMapping("/current/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(
        @AuthenticatedUser User user,
        @RequestBody @Validated(PasswordUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

    @PatchMapping("/current/credentials")
    @ResponseStatus(HttpStatus.OK)
    public void updateCredentials(
        @AuthenticatedUser User user,
        @RequestBody @Validated(CredentialsUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/role-assignment")
    @ResponseStatus(HttpStatus.OK)
    public void addRoleToUser(@RequestBody @Valid UserRolesUpdateDTO userRolesUpdateDTO) {
        User targetUser = userService.getUserById(userRolesUpdateDTO.id());
        userService.addRoleToUser(targetUser, Roles.getRole(userRolesUpdateDTO.role()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/role-unassignment")
    @ResponseStatus(HttpStatus.OK)
    public void removeRoleFromUser(@RequestBody @Valid UserRolesUpdateDTO userRolesUpdateDTO) {
        User targetUser = userService.getUserById(userRolesUpdateDTO.id());
        userService.removeRoleFromUser(targetUser, Roles.getRole(userRolesUpdateDTO.role()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/user-deletion")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@RequestBody @Validated(UserDeletion.class) UserDTO userDTO) {
        User targetUser = userService.getUserById(userDTO.id());
        userService.deleteUser(targetUser.getId());
    }
}
