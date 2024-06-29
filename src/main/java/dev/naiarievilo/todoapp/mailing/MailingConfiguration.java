package dev.naiarievilo.todoapp.mailing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class MailingConfiguration {

    public static final String UTF_8 = StandardCharsets.UTF_8.name();

    private final String appEmail;
    private final String appName;
    private final String domain;
    private final String scheme;
    private final int port;

    public MailingConfiguration(
        @Value("${spring.mail.username}") String appEmail,
        @Value("${spring.application.name}") String appName,
        @Value("${server.address}") String domain,
        @Value("#{${server.ssl.enabled} ? 'https' : 'http'}") String scheme,
        @Value("${server.port}") int port
    ) {
        this.appEmail = appEmail;
        this.appName = appName;
        this.domain = domain;
        this.scheme = scheme;
        this.port = port;
    }

    @Bean
    public ClassLoaderTemplateResolver classLoaderTemplateResolver() {
        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/email");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding(UTF_8);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine thymeleafTemplateEngine(ClassLoaderTemplateResolver classLoaderTemplateResolver) {
        var templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(classLoaderTemplateResolver);
        return templateEngine;
    }

    public String getAppEmail() {
        return appEmail;
    }

    public String getAppName() {
        return appName;
    }

    public String getDomain() {
        return domain;
    }

    public String getScheme() {
        return scheme;
    }

    public int getPort() {
        return port;
    }
}
