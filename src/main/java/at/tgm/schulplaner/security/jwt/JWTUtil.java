package at.tgm.schulplaner.security.jwt;

import at.tgm.schulplaner.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Component
public class JWTUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JWTToken.Generator generator;

    public JWTUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        this.generator = new JWTToken.Generator(secret, expirationTime);
    }

    public JWTToken generateToken(User user) {
        return this.generator.generateToken(user);
    }

    public JWTToken parseToken(String token) {
        return this.generator.parseToken(token);
    }
}
