package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.JwtConstants.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.JwtConstants.REFRESH_TOKEN;
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
        this.jwtVerifier = JWT.require(this.algorithm).build();
        this.issuer = issuer;
    }

    public String createAccessToken(String refreshToken) {
        Validate.notBlank(refreshToken);
        DecodedJWT decodedJwt = jwtVerifier.verify(refreshToken);

        String email = decodedJwt.getSubject();
        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(email)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withExpiresAt(now.plusMillis(accessTokenExpiration.toMillis()));

        return jwtBuilder.sign(algorithm);
    }

    public Map<String, String> createAccessAndRefreshTokens(Authentication authentication) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN, createToken(authentication, TokenTypes.ACCESS_TOKEN));
        tokens.put(REFRESH_TOKEN, createToken(authentication, TokenTypes.REFRESH_TOKEN));
        return tokens;
    }

    private String createToken(Authentication authentication, TokenTypes tokenType) {
        String email = (String) authentication.getPrincipal();

        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(email)
            .withIssuer(issuer)
            .withIssuedAt(now);

        if (tokenType.equals(TokenTypes.ACCESS_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(accessTokenExpiration.toMillis()));
        } else if (tokenType.equals(TokenTypes.REFRESH_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(refreshTokenExpiration.toMillis()));
        }

        return jwtBuilder.sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        Validate.notBlank(token, NOT_BLANK);
        return jwtVerifier.verify(token);
    }

}
