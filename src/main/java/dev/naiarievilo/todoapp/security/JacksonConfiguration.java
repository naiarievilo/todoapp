package dev.naiarievilo.todoapp.security;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule stringSanitizationModule() {
        SimpleModule stringSanitizationModule = new SimpleModule();
        stringSanitizationModule.setDeserializerModifier(new StringDeserializerModifier());
        return stringSanitizationModule;
    }
}
