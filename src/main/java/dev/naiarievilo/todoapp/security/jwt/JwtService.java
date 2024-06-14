package dev.naiarievilo.todoapp.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.users.User;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.IS_BLANK;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final String issuer;
    private final JWTVerifier jwtVerifier;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.jwtVerifier = JWT.require(this.algorithm).build();
        this.issuer = issuer;
    }

    public String createAccessToken(String refreshToken) {
        Validate.notBlank(refreshToken, IS_BLANK, "refreshToken");
        refreshToken = refreshToken.replace(BEARER_PREFIX, "");
        DecodedJWT decodedJwt = this.verifyToken(refreshToken);

        String userId = decodedJwt.getSubject();
        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(userId)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withExpiresAt(now.plusMillis(ACCESS_TOKEN.expirationInMillis()));

        return jwtBuilder.sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        Validate.notBlank(token, IS_BLANK, "token");
        return jwtVerifier.verify(token);
    }

    public Map<String, String> createAccessAndRefreshTokens(User user) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN.key(), createToken(user, ACCESS_TOKEN));
        tokens.put(REFRESH_TOKEN.key(), createToken(user, REFRESH_TOKEN));
        return tokens;
    }

    private String createToken(User user, JwtTokens tokenType) {
        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(user.getId().toString())
            .withIssuer(issuer)
            .withIssuedAt(now);

        if (tokenType.equals(ACCESS_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(ACCESS_TOKEN.expirationInMillis()));
        } else if (tokenType.equals(REFRESH_TOKEN)) {
            jwtBuilder.withExpiresAt(now.plusMillis(REFRESH_TOKEN.expirationInMillis()));
        }

        return jwtBuilder.sign(algorithm);
    }

}
