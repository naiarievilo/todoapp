package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.JwtConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;
    private final String issuer;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expires-in}") Integer accessTokenExpiration,
        @Value("${jwt.refresh-expires-in}") Integer refreshTokenExpiration, @Value("${jwt.issuer}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.accessTokenExpiration = Duration.ofMinutes(accessTokenExpiration);
        this.refreshTokenExpiration = Duration.ofMinutes(refreshTokenExpiration);
        this.verifier = JWT.require(this.algorithm)
            .withClaimPresence(EMAIL_CLAIM)
            .withClaimPresence(ROLES_CLAIM)
            .build();
        this.issuer = issuer;
    }

    public Map<String, String> createAccessAndRefreshTokens(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        Long id = userPrincipal.getId();
        String email = userPrincipal.getEmail();

        String[] roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);

        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(String.valueOf(id))
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withClaim(EMAIL_CLAIM, email)
            .withArrayClaim(ROLES_CLAIM, roles);

        String accessToken = jwtBuilder
            .withExpiresAt(now.plusMillis(accessTokenExpiration.toMillis()))
            .sign(algorithm);

        String refreshToken = jwtBuilder
            .withExpiresAt(now.plusMillis(refreshTokenExpiration.toMillis()))
            .sign(algorithm);

        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN, accessToken);
        tokens.put(REFRESH_TOKEN, refreshToken);

        return tokens;
    }

    public Authentication getAuthentication(String token) throws JWTDecodeException {
        Validate.notBlank(token, NOT_BLANK.message());
        DecodedJWT decodedJWT = verifyToken(token);

        String email = decodedJWT.getClaim(EMAIL_CLAIM).asString();
        List<GrantedAuthority> roles = decodedJWT.getClaim(ROLES_CLAIM).asList(GrantedAuthority.class);
        return EmailPasswordAuthenticationToken.authenticated(email, roles);
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Validate.notBlank(token, NOT_BLANK.message());
        return verifier.verify(token);
    }

}
