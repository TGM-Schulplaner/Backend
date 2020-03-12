package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.Benutzer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-03-12
 */
public interface BenutzerRepository extends ReactiveCrudRepository<Benutzer, UUID> {
    Mono<Benutzer> findByEmail(String email);

    default Mono<Benutzer> getFromUserDetails(UserDetails details) {
        return findByEmail(details.getUsername());
    }
}
