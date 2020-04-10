package at.tgm.schulplaner.security;

import at.tgm.schulplaner.security.jwt.JWTAuthenticationManager;
import at.tgm.schulplaner.security.jwt.JWTAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Slf4j
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final JWTAuthenticationManager jwtAuthManager;

    public SecurityContextRepository(JWTAuthenticationManager jwtAuthManager) {
        this.jwtAuthManager = jwtAuthManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            log.info(request.getLocalAddress().getHostName());
            log.info("{} {}?{}",
                    request.getMethodValue(),
                    request.getPath().value(),
                    request.getQueryParams()
                            .entrySet()
                            .stream()
                            .flatMap(entry -> entry
                                    .getValue()
                                    .stream()
                                    .map(val -> entry.getKey()+"="+val))
                            .collect(Collectors.joining("&")));
            String authToken = authHeader.substring(TOKEN_PREFIX.length());
            Authentication auth = new JWTAuthenticationToken(authToken);
            return this.jwtAuthManager.authenticate(auth).map(SecurityContextImpl::new);
        } else {
            return Mono.empty();
        }
    }

}
