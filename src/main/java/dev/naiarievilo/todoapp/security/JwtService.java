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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.JwtConstants.*;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_BLANK;

@Service
public class JwtService {

    private final Duration accessTokenExpiration;
    private final Algorithm algorithm;
    private final String issuer;
    private final Duration refreshTokenExpiration;
    private final JWTVerifier jwtVerifier;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expires-in}") Integer accessTokenExpiration,
        @Value("${jwt.refresh-expires-in}") Integer refreshTokenExpiration, @Value("${jwt.issuer}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.accessTokenExpiration = Duration.ofMinutes(accessTokenExpiration);
        this.refreshTokenExpiration = Duration.ofMinutes(refreshTokenExpiration);
        this.jwtVerifier = JWT.require(this.algorithm)
            .withClaimPresence(EMAIL_CLAIM)
            .withClaimPresence(ROLES_CLAIM)
            .build();
        this.issuer = issuer;
    }

    public Map<String, String> createAccessAndRefreshTokens(UserPrincipal userPrincipal) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN, createToken(userPrincipal, TokenTypes.ACCESS_TOKEN));
        tokens.put(REFRESH_TOKEN, createToken(userPrincipal, TokenTypes.REFRESH_TOKEN));
        return tokens;
    }

    public String createToken(UserPrincipal userPrincipal, TokenTypes tokenType) {
        String id = String.valueOf(userPrincipal.getId());
        String email = userPrincipal.getEmail();
        String[] roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);

        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(id)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withClaim(EMAIL_CLAIM, email)
            .withArrayClaim(ROLES_CLAIM, roles);

        if (tokenType.equals(TokenTypes.ACCESS_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(accessTokenExpiration.toMillis()));
        }

        if (tokenType.equals(TokenTypes.REFRESH_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(refreshTokenExpiration.toMillis()));
        }

        return jwtBuilder.sign(algorithm);
    }

    public Authentication getAuthentication(String token) throws JWTDecodeException {
        Validate.notBlank(token, NOT_BLANK);
        DecodedJWT decodedJWT = verifyToken(token);

        String email = decodedJWT.getClaim(EMAIL_CLAIM).asString();
        List<SimpleGrantedAuthority> roles = decodedJWT.getClaim(ROLES_CLAIM).asList(SimpleGrantedAuthority.class);
        return EmailPasswordAuthenticationToken.authenticated(email, roles);
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Validate.notBlank(token, NOT_BLANK);
        return jwtVerifier.verify(token);
    }

}
