package dev.naiarievilo.todoapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SecurityScheme(
    name = "Access Token",
    type = SecuritySchemeType.HTTP,
    scheme = "Bearer",
    bearerFormat = "JWT",
    paramName = "AUTHORIZATION",
    description = "After creating or authenticating a user, the API returns the access and refresh tokens through the" +
        " <b>AUTHORIZATION</b> and <b>REFRESH-TOKEN</b> headers, respectively.")
@OpenAPIDefinition(
    info = @Info(
        title = "Todoapp API",
        version = "v1.0",
        description = "Todoapp is a REST API for to-do frontend applications. It features personalized accounts," +
            " mailing service, and to-do lists: inbox list, calendar lists, and custom lists. To-dos have support" +
            " for drag-and-drop behavior and due dates.",
        contact = @Contact(email = "naiarievilo@gmail.com")
    )
)
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry(proxyTargetClass = true)
@EnableScheduling
public class TodoappApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoappApplication.class, args);
    }

}
