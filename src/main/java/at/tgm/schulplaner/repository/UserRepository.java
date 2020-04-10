package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    //@Query("SELECT * FROM user WHERE email = :email")
    Mono<User> findByEmail(@NonNull String email);

    Flux<User> findAllByEmailContainsIgnoreCaseOrNameContainsIgnoreCase(String email, String name, Pageable pageable);

    /*@Query("SELECT group.* FROM member, \"GROUP\" JOIN member m2 on \"GROUP\".id = m2.gid WHERE m2.uid = :id")
    Flux<Group> findGroupsForUser(@NonNull UUID id);*/
}
