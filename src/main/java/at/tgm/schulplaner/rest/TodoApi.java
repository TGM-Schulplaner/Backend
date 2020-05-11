package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.TodoItem;
import at.tgm.schulplaner.model.TodoList;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
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

    @Operation(tags = {"user", "todo"}, summary = "Create a new todo list that belong to the authenticated user", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/user/todo_list")
    public Mono<TodoList> createTodoListForUser(@AuthenticationPrincipal Mono<User> principal, @RequestBody TodoList.NewTodoList newTodoList) {
        return principal.flatMap(u -> dataManager.getTodoListRepo().save(newTodoList.asTodoList(u.getId())));
    }

    @Operation(tags = {"group", "todo"}, summary = "Get all todo lists that belong to the group if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}/todo_lists")
    public Flux<TodoList> getTodoListsForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.READ, DataManager.AccessEntity.TODO_LIST))
                .thenMany(dataManager.getTodoListRepo().findAllByOwner(gid));
    }

    @Operation(tags = {"group", "todo"}, summary = "Create a new todo list that belongs to the group if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/group/{gid}/todo_list")
    public Mono<TodoList> createTodoListForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @RequestBody TodoList.NewTodoList newTodoList) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.CREATE, DataManager.AccessEntity.TODO_LIST))
                .then(dataManager.getTodoListRepo().save(newTodoList.asTodoList(gid)));
    }

    @Operation(tags = {"todo"}, summary = "Get todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/todo_list/{id}")
    public Mono<TodoList> getTodoList(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return dataManager.getTodoList(principal, id, DataManager.AccessType.READ, false);
    }

    @Operation(tags = {"todo"}, summary = "Get todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @PutMapping("/todo_list/{id}")
    public Mono<TodoList> modifyTodoList(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id, @RequestBody TodoList.ModifyTodoList modifyTodoList) {
        return dataManager.getTodoList(principal, id, DataManager.AccessType.MODIFY, false).map(modifyTodoList::modify).flatMap(dataManager.getTodoListRepo()::save);
    }

    @Operation(tags = {"todo"}, summary = "Get todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/todo_list/{id}")
    public Mono<Void> deleteTodoList(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return dataManager.getTodoList(principal, id, DataManager.AccessType.DELETE, false).flatMap(dataManager.getTodoListRepo()::delete);
    }

    @Operation(tags = {"todo"}, summary = "Get all items from todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/todo_list/{id}/items")
    public Flux<TodoItem> getTodoItems(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return principal.filterWhen(user -> dataManager.hasAccessToTodoList(user, id, DataManager.AccessType.READ, true))
                .thenMany(dataManager.getTodoItemRepo().findAllByList(id));
    }

    @Operation(tags = {"todo"}, summary = "Create a new item for todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/todo_list/{id}/item")
    public Mono<TodoItem> createTodoItem(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id, @RequestBody TodoItem.NewTodoItem newTodoItem) {
        return principal.filterWhen(user -> dataManager.hasAccessToTodoList(user, id, DataManager.AccessType.CREATE, true))
                .thenReturn(newTodoItem.asTodoItem(id))
                .flatMap(dataManager.getTodoItemRepo()::save);
    }

    @Operation(tags = {"todo"}, summary = "Modify an item for todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @PutMapping("/todo_list/{listId}/item/{id}")
    public Mono<TodoItem> modifyTodoItem(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID listId, @PathVariable UUID id, @RequestBody TodoItem.ModifyTodoItem modifyTodoItem) {
        return principal.filterWhen(user -> dataManager.hasAccessToTodoList(user, listId, DataManager.AccessType.MODIFY, true))
                .then(dataManager.getTodoItemRepo().findById(id).filter(todoItem -> todoItem.getList().equals(listId)))
                .map(modifyTodoItem::modify)
                .flatMap(dataManager.getTodoItemRepo()::save);
    }

    @Operation(tags = {"todo"}, summary = "Modify an item for todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @PutMapping("/todo_list/{listId}/item/{id}/status")
    public Mono<TodoItem> modifyTodoItem(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID listId, @PathVariable UUID id, @RequestBody TodoItem.ModifyTodoItemStatus modifyTodoItem) {
        return principal.filterWhen(user -> dataManager.hasAccessToTodoList(user, listId, DataManager.AccessType.MODIFY, true))
                .then(dataManager.getTodoItemRepo().findById(id).filter(todoItem -> todoItem.getList().equals(listId)))
                .map(modifyTodoItem::modify)
                .flatMap(dataManager.getTodoItemRepo()::save);
    }

    @Operation(tags = {"todo"}, summary = "Delete item for todo list by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/todo_list/{listId}/item/{id}")
    public Mono<Void> deleteTodoItem(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID listId, @PathVariable UUID id) {
        return principal.filterWhen(user -> dataManager.hasAccessToTodoList(user, listId, DataManager.AccessType.DELETE, true))
                .then(dataManager.getTodoItemRepo().findById(id))
                .filter(todoItem -> todoItem.getList().equals(listId))
                .flatMap(dataManager.getTodoItemRepo()::delete);
    }
}
