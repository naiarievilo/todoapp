package dev.naiarievilo.todoapp.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.users.User;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.*;
import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.USER_ACCESS;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.IS_BLANK;

@Service
public class JwtService {

    public static final String TYPE_CLAIM = "type";

    private final Algorithm algorithm;
    private final String issuer;
    private final JWTVerifier jwtVerifier;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${spring.application.name}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.jwtVerifier = JWT.require(this.algorithm)
            .withIssuer(issuer)
            .withClaimPresence("sub")
            .withClaimPresence("iat")
            .withClaimPresence("exp")
            .withClaimPresence(TYPE_CLAIM)
            .build();
    }

    public String createAccessToken(String refreshToken) {
        Validate.notBlank(refreshToken, IS_BLANK, "refreshToken");
        refreshToken = refreshToken.replace(BEARER_PREFIX, "");
        DecodedJWT decodedJwt = this.verifyToken(refreshToken, USER_ACCESS);

        String userId = decodedJwt.getSubject();
        Instant now = Instant.now();
        JWTCreator.Builder jwtBuilder = JWT.create()
            .withSubject(userId)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withClaim(TYPE_CLAIM, ACCESS_TOKEN.type())
            .withExpiresAt(now.plusMillis(ACCESS_TOKEN.expirationInMillis()));

        return jwtBuilder.sign(algorithm);
    }

    public DecodedJWT verifyToken(String token, TokenTypes tokenType) {
        Validate.notBlank(token, IS_BLANK, "token");

        DecodedJWT verifiedJWT = jwtVerifier.verify(token);
        int typeClaim = verifiedJWT.getClaim(TYPE_CLAIM).asInt();
        if (typeClaim != tokenType.value()) {
            throw new JWTVerificationException("Type claim is not valid");
        }

        return verifiedJWT;
    }

    public Map<String, String> createAccessAndRefreshTokens(User user) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN.key(), createToken(user, ACCESS_TOKEN));
        tokens.put(REFRESH_TOKEN.key(), createToken(user, REFRESH_TOKEN));
        return tokens;
    }

    public String createToken(User user, JwtTokens token) {
        Instant now = Instant.now();
        return JWT.create()
            .withSubject(user.getId().toString())
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withExpiresAt(now.plusMillis(token.expirationInMillis()))
            .withClaim(TYPE_CLAIM, token.type())
            .sign(algorithm);
    }

}
