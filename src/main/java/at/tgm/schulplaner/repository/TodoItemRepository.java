package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.TodoItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TodoItemRepository extends ReactiveCrudRepository<TodoItem, UUID> {
    @Query("SELECT * FROM todo_item WHERE todo_item.todo_list = :list")
    Flux<TodoItem> findAllByList(@NonNull UUID list);
}
