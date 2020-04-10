package at.tgm.schulplaner.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
public class JWTAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;

    public JWTAuthenticationToken(String token) {
        super(Collections.emptyList());
        this.token = token;
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public String getPrincipal() {
        return token;
    }
}
