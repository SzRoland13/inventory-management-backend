package dev.roland.inventory_management_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(User user) throws IllegalArgumentException, JWTCreationException {

        return JWT.create()
                .withSubject("User Details")
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().toString())
                .withIssuedAt(new Date())
                .withIssuer("BILLING APPLICATION")
                .withExpiresAt(Date.from(LocalDateTime.now()
                        .plusMinutes(15)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("BILLING APPLICATION")
                .build();

        return verifier.verify(token);
    }

    public String getUsername(String token) {
        return validateToken(token).getClaim("username").asString();
    }

    public UserRole getRole(String token) {
        String roleClaim = validateToken(token).getClaim("role").asString();

        return UserRole.fromString(roleClaim)
                .orElseThrow(() -> new RuntimeException("Invalid role: " + roleClaim));

    }
}
