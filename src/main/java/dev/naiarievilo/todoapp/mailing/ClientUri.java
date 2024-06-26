package dev.naiarievilo.todoapp.mailing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("client.uri")
public record ClientUri(
    String unlockUser
) { }
