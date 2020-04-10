package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.Group;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface GroupRepository extends ReactiveCrudRepository<Group, UUID> {
    /*@Query("SELECT * FROM \"GROUP\"")
    @Override
    Flux<Group> findAll();*/
}
