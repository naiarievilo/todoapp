package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        Authentication authentication = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateUser(@Valid @RequestBody UserAuthenticationDTO userAuthenticationDTO) {
        var authenticatedToken = authenticationManager.authenticate(EmailPasswordAuthenticationToken.unauthenticated(
            userAuthenticationDTO.email(), userAuthenticationDTO.password()));

        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authenticatedToken);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PutMapping("/re-authenticate")
    public ResponseEntity<Void> getNewAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
        String newAccessToken = jwtService.createAccessToken(refreshToken);
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
            .build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication userAuthenticated = context.getAuthentication();

        userService.deleteUser(String.valueOf(userAuthenticated.getPrincipal()));
        return ResponseEntity.noContent().build();
    }
}
