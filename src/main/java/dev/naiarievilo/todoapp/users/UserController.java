package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalAuthenticationToken;
import dev.naiarievilo.todoapp.users.dtos.CreateUserDTO;
import dev.naiarievilo.todoapp.users.dtos.UpdateCredentialsDTO;
import dev.naiarievilo.todoapp.users.dtos.UserDTO;
import dev.naiarievilo.todoapp.users.dtos.groups.AuthenticateUser;
import dev.naiarievilo.todoapp.users.dtos.groups.UpdateEmail;
import dev.naiarievilo.todoapp.users.dtos.groups.UpdatePassword;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Void> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        UserPrincipal userPrincipal = userService.createUser(createUserDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateUser(@RequestBody @Validated(AuthenticateUser.class) UserDTO userDTO) {
        var authentication = (UserPrincipalAuthenticationToken) authenticationManager.authenticate(
            EmailPasswordAuthenticationToken.unauthenticated(userDTO.email(), userDTO.password())
        );

        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication.getPrincipal());
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PutMapping("/reauthenticate")
    public ResponseEntity<Void> getNewAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
        String newAccessToken = jwtService.createAccessToken(refreshToken);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            .build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUser(userPrincipal);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-email")
    public ResponseEntity<Void> updateEmail(@AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestBody @Validated(UpdateEmail.class) UpdateCredentialsDTO updateCredentialsDTO) {
        userService.updateEmail(userPrincipal, updateCredentialsDTO.newEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestBody @Validated(UpdatePassword.class) UpdateCredentialsDTO updateCredentialsDTO) {
        userService.updatePassword(userPrincipal, updateCredentialsDTO.currentPassword(),
            updateCredentialsDTO.newPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
