package dev.naiarievilo.todoapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {MethodValidationPostProcessor.class, LocalValidatorFactoryBean.class})
class PasswordMatchingIntegrationTests {

    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    @DisplayName("@PasswordMatching: Returns constraint violation error when passwords don't match")
    void passwordMatching_PasswordsDoNotMatch_DoesNotReturnConstraintViolationError() {
        TestDTO testDTO = new TestDTO("password", "differentPassword");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertTrue(errors.hasErrors());
    }

    @Test
    @DisplayName("@PasswordMatching: Does not return constraint violation error when passwords match")
    void passwordMatching_PasswordsMatch_ReturnsConstraintViolationError() {
        TestDTO testDTO = new TestDTO("password", "password");
        Errors errors = new BeanPropertyBindingResult(testDTO, "testDTO");

        localValidatorFactoryBean.validate(testDTO, errors);
        assertFalse(errors.hasErrors());
    }

    @PasswordMatching(password = "password", confirmPassword = "confirmPassword")
    record TestDTO(
        String password,
        String confirmPassword
    ) {

    }
}
