package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.JwtService;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        UserPrincipal userPrincipal = userService.createUser(userCreationDTO);
        String token = jwtService.createToken(userPrincipal);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Authorization", "Bearer " + token)
            .build();
    }
}
