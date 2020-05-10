package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.TodoItem;
import at.tgm.schulplaner.model.TodoList;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TodoApi {
    private final DataManager dataManager;

    @Operation(tags = {"user", "todo"}, summary = "Get all todo lists that belong to the authenticated user", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/user/todo_lists")
    public Flux<TodoList> getTodoListsForUser(@AuthenticationPrincipal Mono<User> principal) {
        return principal.flatMapMany(u -> dataManager.getTodoListRepo().findAllByOwner(u.getId()));
    }

    @Operation(tags = {"group", "todo"}, summary = "Get all todo lists that belong to the group if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}/todo_lists")
    public Flux<TodoList> getTodoListsForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return dataManager.filterGroupID(principal, gid).flatMapMany(dataManager.getTodoListRepo()::findAllByOwner);
    }

    @Operation(tags = {"todo"}, summary = "Get todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/todo_list/{id}")
    public Mono<TodoList> getTodoList(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return principal
                .flatMap(u -> dataManager.getTodoListRepo()
                        .findById(id)
                        .filterWhen(todoList -> dataManager.hasAccess(u, todoList)));
    }

    @Operation(tags = {"todo"}, summary = "Get all items from todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/todo_list/{id}/items")
    public Flux<TodoItem> getTodoItems(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return getTodoList(principal, id)
                .map(TodoList::getId)
                .flatMapMany(dataManager.getTodoItemRepo()::findAllByList);
    }
}
