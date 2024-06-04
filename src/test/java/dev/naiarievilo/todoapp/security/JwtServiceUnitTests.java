package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.users.User;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.security.JwtConstants.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUnitTests {

    private static final Integer JWT_EXPIRATION = 30;
    private static final String JWT_ISSUER = "testApp";
    private static final Integer JWT_REFRESH_EXPIRATION = 60;
    private static final String JWT_SECRET = "jwtSecret";

    private final JwtService jwtService;
    private final JWTVerifier jwtVerifier;

    private UserPrincipal userPrincipal;

    JwtServiceUnitTests() {
        jwtService = new JwtService(JWT_SECRET, JWT_EXPIRATION, JWT_REFRESH_EXPIRATION, JWT_ISSUER);

        try {
            Field jwtVerifierField = JwtService.class.getDeclaredField("jwtVerifier");
            jwtVerifierField.setAccessible(true);
            this.jwtVerifier = (JWTVerifier) jwtVerifierField.get(jwtService);
            assertNotNull(jwtVerifier);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName(ROLE_USER.name());

        User user = new User();
        user.setId(ID);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.addRole(userRole);
        user.setIsEnabled(true);
        user.setIsLocked(false);

        userPrincipal = UserPrincipalImpl.withUser(user);
    }

    @Test
    @DisplayName("createAccessAndRefreshTokens(): Returns access and refresh tokens when user principal is not null")
    void createAccessAndRefreshTokens_UserPrincipalIsNotNull_CreatesAccessAndRefreshTokens() {
        Long id = userPrincipal.getId();
        String email = userPrincipal.getEmail();
        List<String> roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertTrue(tokens.containsKey(ACCESS_TOKEN));
        assertTrue(tokens.containsKey(REFRESH_TOKEN));

        String accessToken = tokens.get(ACCESS_TOKEN);
        assertDoesNotThrow(() -> Validate.notBlank(accessToken));
        String refreshToken = tokens.get(REFRESH_TOKEN);
        assertDoesNotThrow(() -> Validate.notBlank(refreshToken));

        assertDoesNotThrow(() -> jwtVerifier.verify(accessToken));
        DecodedJWT decodedAccessToken = jwtVerifier.verify(accessToken);
        assertDoesNotThrow(() -> jwtVerifier.verify(refreshToken));
        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);

        assertEquals(id, Long.valueOf(decodedAccessToken.getSubject()));
        assertEquals(id, Long.valueOf(decodedRefreshToken.getSubject()));

        assertEquals(JWT_ISSUER, decodedAccessToken.getIssuer());
        assertEquals(JWT_ISSUER, decodedRefreshToken.getIssuer());

        assertEquals(email, decodedAccessToken.getClaim(EMAIL_CLAIM).asString());
        assertEquals(email, decodedRefreshToken.getClaim(EMAIL_CLAIM).asString());

        assertEquals(roles, decodedAccessToken.getClaim(ROLES_CLAIM).asList(String.class));
        assertEquals(roles, decodedRefreshToken.getClaim(ROLES_CLAIM).asList(String.class));

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();

        Instant refreshTokenIssuedAt = decodedRefreshToken.getIssuedAtAsInstant();
        Instant refreshTokenExpiresAt = decodedRefreshToken.getExpiresAtAsInstant();

        assertEquals(accessTokenIssuedAt, refreshTokenIssuedAt);
        assertTrue(accessTokenExpiresAt.isBefore(refreshTokenExpiresAt));
        assertEquals(Duration.ofMinutes(JWT_EXPIRATION), Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
        assertEquals(Duration.ofMinutes(JWT_REFRESH_EXPIRATION),
            Duration.between(refreshTokenIssuedAt, refreshTokenExpiresAt));
    }

    @Test
    @DisplayName("createAccessToken(): Creates access token when refresh token is valid")
    void createAccessToken_RefreshTokenIsValid_CreatesAccessToken() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);
        String refreshToken = tokens.get(REFRESH_TOKEN);

        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);
        Long id = Long.valueOf(decodedRefreshToken.getSubject());
        String email = decodedRefreshToken.getClaim(EMAIL_CLAIM).asString();
        List<String> roles = decodedRefreshToken.getClaim(ROLES_CLAIM).asList(String.class);

        String newAccessToken = jwtService.createAccessToken(refreshToken);
        assertDoesNotThrow(() -> Validate.notBlank(newAccessToken));

        assertDoesNotThrow(() -> jwtVerifier.verify(newAccessToken));
        DecodedJWT decodedAccessToken = jwtVerifier.verify(newAccessToken);

        assertEquals(id, Long.valueOf(decodedAccessToken.getSubject()));
        assertEquals(email, decodedAccessToken.getClaim(EMAIL_CLAIM).asString());
        assertEquals(decodedRefreshToken.getIssuer(), decodedAccessToken.getIssuer());

        List<String> accessTokenRoles = decodedAccessToken.getClaim(ROLES_CLAIM).asList(String.class);
        assertTrue(roles.containsAll(accessTokenRoles) && accessTokenRoles.containsAll(roles));

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();
        assertEquals(Duration.ofMinutes(JWT_EXPIRATION), Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
    }

    @Test
    @DisplayName("getAuthentication(): Throws `JWTDecodeException` when token is not valid")
    void getAuthentication_TokenIsNotValid_ThrowsJWTDecodeException() {
        assertThrows(JWTDecodeException.class, () -> jwtService.getAuthentication("notValidToken"));
    }

    @Test
    @DisplayName("getAuthentication(): Returns `Authentication` when token is valid")
    void getAuthentication_TokenIsValid_ReturnsAuthentication() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);
        String accessToken = tokens.get(ACCESS_TOKEN);

        Authentication authentication = jwtService.getAuthentication(accessToken);
        assertInstanceOf(EmailPasswordAuthenticationToken.class, authentication);
        assertEquals(userPrincipal.getEmail(), authentication.getPrincipal());
        assertEquals(userPrincipal.getAuthorities().size(), authentication.getAuthorities().size());
        assertTrue(userPrincipal.getAuthorities().containsAll(authentication.getAuthorities()));
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    @DisplayName("verifyToken(): Throws `JWTVerificationException` when token is not valid")
    void verifyToken_TokenIsNotValid_ThrowsJWTVerificationException() {
        assertThrows(JWTVerificationException.class, () -> jwtService.verifyToken("notValidToken"));
    }

    @Test
    @DisplayName("verifyToken(): Returns `DecodedJWT` when token is valid")
    void verifyToken_TokenIsValid_ReturnsDecodedJWT() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(userPrincipal);
        String accessToken = tokens.get(ACCESS_TOKEN);

        DecodedJWT decodedJwt = jwtService.verifyToken(accessToken);
        assertNotNull(decodedJwt);
    }

}
