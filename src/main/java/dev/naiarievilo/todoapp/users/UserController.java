package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.naiarievilo.todoapp.security.JwtConstants.*;


@RestController
@RequestMapping("/users")
public class UserController {

    private static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        UserPrincipal userPrincipal = userService.createUser(userCreationDTO);
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, tokens.get(REFRESH_TOKEN))
            .build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateUser(@Valid @RequestBody UserAuthenticationDTO userAuthenticationDTO) {
        authenticationManager.authenticate(EmailPasswordAuthenticationToken.unauthenticated(
            userAuthenticationDTO.email(), userAuthenticationDTO.password()
        ));

        UserPrincipal userPrincipal = userService.loadUserByEmail(userAuthenticationDTO.email());
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshUserAccessToken(HttpServletRequest request) {
        DecodedJWT decodedJWT = jwtService.verifyToken(request.getHeader(HttpHeaders.AUTHORIZATION));

        UserPrincipal userPrincipal =
            userService.loadUserByEmail(decodedJWT.getClaim(EMAIL_CLAIM).asString());
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokens.get(ACCESS_TOKEN))
            .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + tokens.get(REFRESH_TOKEN))
            .build();
    }
}
