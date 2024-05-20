package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

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
        String token = jwtService.createToken(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Authorization", "Bearer " + token)
            .build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateUser(@Valid @RequestBody UserAuthenticationDTO userAuthenticationDTO) {
        authenticationManager.authenticate(EmailPasswordAuthenticationToken.unauthenticated(
            userAuthenticationDTO.email(), userAuthenticationDTO.password()
        ));

        UserPrincipal userPrincipal = userService.loadUserByEmail(userAuthenticationDTO.email());
        String token = jwtService.createToken(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header("Authorization", "Bearer " + token)
            .build();
    }
}
