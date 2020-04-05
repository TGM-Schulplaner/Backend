package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.TodoList;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TodoListRepository extends ReactiveCrudRepository<TodoList, UUID> {
}
