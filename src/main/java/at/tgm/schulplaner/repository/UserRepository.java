package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * @author Georg Burkl
 * @version 2020-03-11
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> getByUsername(String username);
}
