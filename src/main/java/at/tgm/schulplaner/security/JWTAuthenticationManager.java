package at.tgm.schulplaner.security;

import at.tgm.schulplaner.repository.UserRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Component
public class JWTAuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JWTAuthenticationManager(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        String username;
        try {
            username = jwtUtil.getUsernameFromToken(authToken);
        } catch (Exception e) {
            username = null;
        }
        if (username != null && jwtUtil.validateToken(authToken)) {
            return userRepository.findByEmail(username).map(user -> new UsernamePasswordAuthenticationToken(user, null));
        } else {
            return Mono.empty();
        }
    }
}
