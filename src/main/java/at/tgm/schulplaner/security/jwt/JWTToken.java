package at.tgm.schulplaner.security.jwt;

import at.tgm.schulplaner.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Georg Burkl
 * @version 2020-04-09
 */
public class JWTToken {
    public static class Generator {
        private final String secret;
        private final long expirationTime;

        Generator(@Value("${jwt.secret}") String secret,
                  @Value("${jwt.expiration}") long expirationTime) {
            this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
            this.expirationTime = expirationTime;
        }

        public JWTToken generateToken(User user) {
            return parseToken(doGenerateToken(user));
        }

        public JWTToken parseToken(String token) {
            return new JWTToken(token, getAllClaimsFromToken(token));
        }

        private String doGenerateToken(User user) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getType());
            claims.put("uuid", user.getId());
            return doGenerateToken(claims, user.getUsername());
        }

        private String doGenerateToken(Map<String, Object> claims, String username) {
            final Date createdDate = new Date();
            final Date expirationDate = new Date(createdDate.getTime() + expirationTime * 1000);
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(createdDate)
                    .setExpiration(expirationDate)
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
        }

        private Claims getAllClaimsFromToken(String token) {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        }
    }

    private final String token;
    private final Claims claimsCache;

    private JWTToken(String token, Claims claims) {
        this.token = token;
        this.claimsCache = claims;
    }

    public String getTokenString() {
        return token;
    }

    public String getUsername() {
        return claimsCache.getSubject();
    }

    public Date getExpirationDate() {
        return claimsCache.getExpiration();
    }

    public boolean isTokenExpired() {
        final Date expiration = getExpirationDate();
        return expiration.before(new Date());
    }

    public boolean validateToken() {
        return !isTokenExpired();
    }
}
