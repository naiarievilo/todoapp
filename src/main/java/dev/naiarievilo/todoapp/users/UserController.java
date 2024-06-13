package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.AuthenticatedUser;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.security.UserAuthenticationToken;
import dev.naiarievilo.todoapp.users.dtos.CredentialsUpdateDTO;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.dtos.groups.CredentialsUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.EmailUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.PasswordUpdate;
import dev.naiarievilo.todoapp.users.dtos.groups.UserAuthentication;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static dev.naiarievilo.todoapp.security.JwtConstants.*;

@RestController
@RequestMapping("/users")
public class UserController {

    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        User user = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateUser(@RequestBody @Validated(UserAuthentication.class) UserDTO userDTO) {
        var authentication = (UserAuthenticationToken) authenticationManager.authenticate(
            EmailPasswordAuthenticationToken.unauthenticated(userDTO.email(), userDTO.password())
        );

        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication.getPrincipal());
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PostMapping("/reauthenticate")
    public ResponseEntity<Void> getNewAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
        String newAccessToken = jwtService.createAccessToken(refreshToken);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            .build();
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticatedUser User user) {
        userService.deleteUser(user);
    }

    @PatchMapping("/update-email")
    @ResponseStatus(HttpStatus.OK)
    public void updateEmail(
        @AuthenticatedUser User user,
        @RequestBody @Validated(EmailUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
    }

    @PatchMapping("/update-password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(
        @AuthenticatedUser User user,
        @RequestBody @Validated(PasswordUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

    @PatchMapping("/update-credentials")
    @ResponseStatus(HttpStatus.OK)
    public void updateCredentials(
        @AuthenticatedUser User user,
        @RequestBody @Validated(CredentialsUpdate.class) CredentialsUpdateDTO newCredentials
    ) {
        userService.updateEmail(user, newCredentials.newEmail());
        userService.updatePassword(user, newCredentials.currentPassword(), newCredentials.newPassword());
    }

}
