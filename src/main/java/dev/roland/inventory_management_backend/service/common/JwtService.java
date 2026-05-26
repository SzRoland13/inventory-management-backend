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

  /**
   * Extracts the username stored as the JWT subject.
   *
   * @param token signed JWT
   * @return username from the token subject
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the role claim from a JWT.
   *
   * @param token signed JWT
   * @return role claim value
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Extracts a claim value from a JWT using the supplied resolver.
   *
   * @param token signed JWT
   * @param claimsResolver function that reads the desired value from parsed claims
   * @param <T> extracted claim value type
   * @return resolved claim value
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);

    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Generates an access token with the user's role and email claims.
   *
   * @param user user to issue the token for
   * @return signed access token
   */
  public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole().name());
    claims.put("email", user.getEmail());
    return generateAccessToken(claims, user);
  }

  /**
   * Generates an access token with additional claims.
   *
   * @param extraClaims claims to include in the token payload
   * @param user user to issue the token for
   * @return signed access token
   */
  public String generateAccessToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, appConfiguration.getAccessTokenExpirationTime());
  }

  /**
   * Generates a refresh token with additional claims.
   *
   * @param extraClaims claims to include in the token payload
   * @param user user to issue the token for
   * @return signed refresh token
   */
  public String generateRefreshToken(Map<String, Object> extraClaims, User user) {
    return buildToken(extraClaims, user, appConfiguration.getRefreshTokenExpirationTime());
  }

  /**
   * Generates a refresh token without extra claims.
   *
   * @param user user to issue the token for
   * @return signed refresh token
   */
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

  /**
   * Checks whether a token belongs to the supplied user and is not expired.
   *
   * @param token signed JWT
   * @param user expected token owner
   * @return true when the token subject matches the user and the token is not expired
   */
  public boolean isTokenValid(String token, User user) {
    final String username = extractUsername(token);
    return (username.equals(user.getUsername())) && !isTokenExpired(token);
  }

  /**
   * Checks whether a token belongs to the supplied Spring Security user and is not expired.
   *
   * @param token signed JWT
   * @param userDetails expected token owner
   * @return true when the token subject matches the user details and the token is not expired
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  /**
   * Checks whether a JWT has passed its expiration timestamp.
   *
   * @param token signed JWT
   * @return true when the token is expired
   */
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
