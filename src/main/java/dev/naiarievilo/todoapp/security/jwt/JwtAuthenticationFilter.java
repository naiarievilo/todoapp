package dev.naiarievilo.todoapp.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.UserAuthenticationToken;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.BEARER_PREFIX;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.replaceFirst(BEARER_PREFIX, "");
        User user;
        try {
            DecodedJWT verifiedJWT = jwtService.verifyToken(token);
            Long userId = Long.valueOf(verifiedJWT.getSubject());
            user = userService.getUserById(userId);

        } catch (JWTVerificationException e) {
            buildJwtErrorDetailsResponse(response);
            return;

        } catch (UserNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
            response.getWriter().print(objectMapper.writeValueAsString(errorDetails));
            return;
        }

        if (userService.isUserExpired(user)) {
            userService.deleteUser(user);
            this.buildJwtErrorDetailsResponse(response);
            return;

        } else if (userService.isUserInactive(user)) {
            this.buildJwtErrorDetailsResponse(response);
            return;
        }

        var authentication = new UserAuthenticationToken(user);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        filterChain.doFilter(request, response);
    }

    private void buildJwtErrorDetailsResponse(HttpServletResponse response) throws IOException {
        var errorDetails = new ErrorDetails(HttpStatus.UNAUTHORIZED, JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(objectMapper.writeValueAsString(errorDetails));
    }
}
