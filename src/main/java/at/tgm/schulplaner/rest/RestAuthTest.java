package at.tgm.schulplaner.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Georg Burkl
 * @version 2020-03-07
 */
@Slf4j
@RestController
public class RestAuthTest {

    @GetMapping("/")
    public Mono<Object> greet(Mono<Authentication> principal) {
        return principal.map(Authentication::getPrincipal);
    }
}
