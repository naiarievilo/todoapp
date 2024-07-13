package dev.naiarievilo.todoapp.roles;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.ControllerIntegrationTests;
import dev.naiarievilo.todoapp.roles.dtos.RoleDTO;
import dev.naiarievilo.todoapp.roles.dtos.RolesDTO;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.naiarievilo.todoapp.roles.RoleControllerTestCases.STATUS_200_GETS_ALL_ROLES_WHEN_ROLES_PERSISTED_IN_DATABASE;
import static dev.naiarievilo.todoapp.roles.RoleControllerTestCases.STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.BEARER_PREFIX;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerIT extends ControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    JwtService jwtService;
    @Autowired
    RoleService roleService;
    @Autowired
    ObjectMapper objectMapper;

    @Value("${admin.email}")
    private String adminEmail;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
    }

    @Test
    @DisplayName("getAllRoles(): " + STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN)
    void getAllRoles_AuthenticatedUserNotAdmin_ReturnsForbidden() throws Exception {
        User newUser = userService.createUser(userCreationDTO);
        String accessToken = jwtService.createAccessAndRefreshTokens(newUser).get(ACCESS_TOKEN.key());

        mockMvc.perform(get("/roles")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getAllRoles(): " + STATUS_200_GETS_ALL_ROLES_WHEN_ROLES_PERSISTED_IN_DATABASE)
    void getAllRoles_RolesInDatabase_ReturnsAllRoles() throws Exception {
        User adminUser = userService.getUserByEmail(adminEmail);
        String accessToken = jwtService.createAccessAndRefreshTokens(adminUser).get(ACCESS_TOKEN.key());
        List<RoleDTO> expectedRoles = roleService.getAllRoles().stream()
            .map(role -> new RoleDTO(role.getName(), role.getDescription()))
            .toList();

        String responseBody = mockMvc.perform(get("/roles")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        RolesDTO rolesDTO = objectMapper.readValue(responseBody, RolesDTO.class);
        assertTrue(rolesDTO.roles().containsAll(expectedRoles));
    }
}
