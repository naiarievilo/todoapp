package dev.naiarievilo.todoapp.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static dev.naiarievilo.todoapp.users.UsersTestConstants.EMAIL;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.PASSWORD;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class UserRepositoryIntegrationTests {

    @Autowired
    UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
    }

    @Test
    @DisplayName("findByEmail(): Returns empty `Optional<User>` when user does not exist")
    void findByEmail_UserDoesNotExist_ReturnsEmptyOptional() {
        assertTrue(userRepository.findByEmail(user.getEmail()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("findByEmail(): Returns populated `Optional<User>` when user exists")
    void findByEmail_UserExists_ReturnsUser() {
        userRepository.persist(user);
        assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("deleteByEmail(): Deletes `User` when user exists")
    void deleteByEmail_UserExists_DeletesUser() {
        userRepository.persist(user);
        assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
        userRepository.delete(user);
        assertTrue(userRepository.findByEmail(user.getEmail()).isEmpty());
    }

}
