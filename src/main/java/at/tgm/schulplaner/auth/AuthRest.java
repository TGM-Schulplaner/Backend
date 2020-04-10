package at.tgm.schulplaner.auth;

import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Georg Burkl
 * @version 2020-04-08
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthRest {

    private final ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider;
    private final JWTUtil jwtUtil;

    @PostMapping("/api/v1/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody Mono<AuthRequest> request) {
        return request.map(authRequest ->
                ResponseEntity.ok(
                        new AuthResponse(
                                jwtUtil.generateToken(
                                        (User) ldapAuthenticationProvider.authenticate(
                                                new UsernamePasswordAuthenticationToken(
                                                        authRequest.getUsername(),
                                                        authRequest.getPassword()))
                                                .getPrincipal())
                                        .getTokenString())))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
