package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    @Query("SELECT * FROM user WHERE email = :email")
    Mono<User> findByEmail(@NonNull String email);
}
