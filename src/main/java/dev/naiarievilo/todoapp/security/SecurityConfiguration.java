package dev.naiarievilo.todoapp.security;

import dev.naiarievilo.todoapp.mailing.EmailService;
import dev.naiarievilo.todoapp.security.jwt.JwtAuthenticationFilter;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String DEV_OR_TEST = "dev || test";

    @Bean
    @Profile(DEV_OR_TEST)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
        throws Exception {

        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/actuator/**", "/users/authentication", "/users/enable", "/users/unlock",
                    "/api-docs", "/swagger-ui/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/users/{userId}/re-authentication", "/users/{userId}/verification",
                    "/users/{userId}/unlock", "/users/{userId}/enable"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers("/users/{userId}/**").access(userAuthorizationManager())
                .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthorizationManager<RequestAuthorizationContext> userAuthorizationManager() {
        return new AuthorizationManager<RequestAuthorizationContext>() {

            @Override
            public void verify(
                Supplier<Authentication> authentication,
                RequestAuthorizationContext context
            ) {
                AuthorizationDecision decision = this.check(authentication, context);
                if (decision == null || !decision.isGranted()) {
                    throw new AccessDeniedException("Access to resource denied");
                }
            }

            @Override
            public AuthorizationDecision check(
                Supplier<Authentication> authentication,
                RequestAuthorizationContext context
            ) {
                Object principal = authentication.get().getPrincipal();
                if (!(principal instanceof User authenticatedUser)) {
                    return null;
                }

                Map<String, String> pathVariables = context.getVariables();
                Long userId = Long.valueOf(pathVariables.get("userId"));

                return new AuthorizationDecision(userId.equals(authenticatedUser.getId()));
            }
        };
    }

    @Bean
    @Profile(DEV_OR_TEST)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserService userService, EmailService emailService,
        PasswordEncoder passwordEncoder) {
        var jwtAuthenticationProvider = new EmailPasswordAuthenticationProvider(
            userService, emailService, passwordEncoder
        );
        return new ProviderManager(jwtAuthenticationProvider);
    }
}

