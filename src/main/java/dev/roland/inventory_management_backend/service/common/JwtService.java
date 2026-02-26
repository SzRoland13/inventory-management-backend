package dev.roland.inventory_management_backend.service.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {
  private final AppConfiguration appConfiguration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);

    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole().name());
    claims.put("email", user.getEmail());
    return generateAccessToken(claims, user);
  }

  public String generateAccessToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, appConfiguration.getAccessTokenExpirationTime());
  }

  public String generateRefreshToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, appConfiguration.getRefreshTokenExpirationTime());
  }

  public String generateRefreshToken(User user) {
    return generateRefreshToken(new HashMap<>(), user);
  }

  private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
    long now = System.currentTimeMillis();

    return Jwts.builder()
        .claims(extraClaims)
        .subject(user.getUsername())
        .issuedAt(new Date(now))
        .expiration(new Date(now + expiration))
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, User user) {
    final String username = extractUsername(token);
    return (username.equals(user.getUsername())) && !isTokenExpired(token);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(appConfiguration.getSecret());

    return Keys.hmacShaKeyFor(keyBytes);
  }
}
