package dev.naiarievilo.todoapp.mailing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("client.uri")
public record ClientUriProperties(
    String unlockUser
) { }
