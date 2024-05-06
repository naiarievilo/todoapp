package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final Duration expiration;
    private final String issuer;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Integer expiration,
        @Value("${jwt.issuer}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(this.algorithm).build();
        this.expiration = Duration.ofMinutes(expiration);
        this.issuer = issuer;
    }

    public String createToken(UserPrincipal userPrincipal) {
        String username = userPrincipal.getUsername();
        String email = userPrincipal.getEmail();

        String[] roles = userPrincipal.getRoles()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);
        String[] authorities = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);

        Instant now = Instant.now();
        return JWT.create()
            .withSubject(username)
            .withIssuer(issuer)
            .withIssuedAt(now)
            .withExpiresAt(now.plusMillis(expiration.toMillis()))
            .withClaim("email", email)
            .withArrayClaim("roles", roles)
            .withArrayClaim("authorities", authorities)
            .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }
}