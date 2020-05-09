/*
 * Copyright (c) 2020 tgm - Die Schule der Technik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.dto.UserDTO;
import at.tgm.schulplaner.model.*;
import at.tgm.schulplaner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-03-30
 */
@SuppressWarnings({"ConstantConditions", "ReactiveStreamsNullableInLambdaInTransform"})
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET,
        RequestMethod.DELETE,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.OPTIONS,
        RequestMethod.PATCH
})
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RestApi {
    //region repositories

    private @Value("${admin_accounts}") Collection<String> sysAdmins;
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final CalendarRepository calendarRepo;
    private final CalendarEntryRepository calendarEntryRepository;
    private final TodoListRepository todoListRepo;
    private final TodoItemRepository todoItemRepo;

    //endregion
    //region user
    @GetMapping("/users")
    public Flux<UserDTO> searchForUser(Mono<Authentication> principal, @RequestParam(value = "q", required = false, defaultValue = "") String q, Pageable pageable) {
        return principal
                .flatMapMany(p -> userRepo.findAllByEmailContainsIgnoreCaseOrNameContainsIgnoreCase(q, q, pageable)
                        .map(UserDTO::new));
    }

    @GetMapping("/user")
    public Mono<UserDTO> getUserInfo(Mono<Authentication> principal) {
        return user(principal).map(UserDTO::new);
    }

    @GetMapping("/user/calendars")
    public Flux<Calendar> getCalendarsForUser(Mono<Authentication> principal) {
        return user(principal).flatMapMany(u -> calendarRepo.findAllByOwner(u.getId()));
    }

    @GetMapping("/user/todo_lists")
    public Flux<TodoList> getTodoListsForUser(Mono<Authentication> principal) {
        return user(principal).flatMapMany(u -> todoListRepo.findAllByOwner(u.getId()));
    }
    //endregion
    //region group
    @GetMapping("/groups")
    public Flux<Group> getGroups(Mono<Authentication> principal) {
        return user(principal).flatMapMany(user -> groupRepo.findAll().filter(group -> memberRepo.existsByUidAndGid(user.getId(), group.getId())));
    }

    @GetMapping("/group/{gid}")
    public Mono<Group> getGroup(Mono<Authentication> principal, @PathVariable UUID gid) {
        return user(principal)
                .flatMap(user -> memberRepo.getByUidAndGid(user.getId(), gid))
                .map(Member::getGid)
                .flatMap(groupRepo::findById);
    }

    @GetMapping("/group/{gid}/users")
    public Flux<UserDTO> getGroupUsers(Mono<Authentication> principal, @PathVariable UUID gid) {
        return getGroup(principal, gid)
                .map(Group::getId)
                .flatMapMany(memberRepo::getAllByGid)
                .map(Member::getUid)
                .flatMap(userRepo::findById)
                .map(UserDTO::new);
    }

    @GetMapping("/group/{gid}/calendars")
    public Flux<Calendar> getCalendarsForGroup(Mono<Authentication> principal, @PathVariable UUID gid) {
        return getGroup(principal, gid)
                .map(Group::getId)
                .flatMapMany(calendarRepo::findAllByOwner);
    }

    @GetMapping("/group/{gid}/todo_lists")
    public Flux<TodoList> getTodoListsForGroup(Mono<Authentication> principal, @PathVariable UUID gid) {
        return getGroup(principal, gid)
                .map(Group::getId)
                .flatMapMany(todoListRepo::findAllByOwner);
    }
    //endregion
    //region calendar
    @GetMapping("/calendar/{id}")
    public Mono<Calendar> getCalendar(Mono<Authentication> principal, @PathVariable UUID id) {
        return user(principal).flatMap(u -> calendarRepo
                .findById(id)
                .filterWhen(calendar -> hasAccess(u, calendar)));
    }

    @GetMapping("/calendar/{id}/entries")
    public Flux<CalendarEntry> getCalendarEntries(Mono<Authentication> principal,
                                                  @PathVariable UUID id,
                                                  @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                  @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Mono<UUID> calendarId = getCalendar(principal, id).map(Calendar::getId);
        if (start != null && end != null) {
            return calendarId.flatMapMany(c -> calendarEntryRepository
                    .findAllByCalendarAndStartIsLessThanEqualAndEndIsGreaterThanEqual(c, end, start));
        }
        return calendarId.flatMapMany(calendarEntryRepository::findAllByCalendar);
    }
    //endregion
    //region todo
    @GetMapping("/todo_list/{id}")
    public Mono<TodoList> getTodoList(Mono<Authentication> principal, @PathVariable UUID id) {
        return user(principal).flatMap(u -> todoListRepo
                .findById(id)
                .filterWhen(todoList -> hasAccess(u, todoList)));
    }

    @GetMapping("/todo_list/{id}/items")
    public Flux<TodoItem> getTodoItems(Mono<Authentication> principal, @PathVariable UUID id) {
        return getTodoList(principal, id).map(TodoList::getId).flatMapMany(todoItemRepo::findAllByList);
    }
    //endregion
    //region helpers

//    private Mono<User> ifGlobalAdmin(Mono<User> user) {
//        return user.filter(this::isGlobalAdmin);
//    }

    private boolean isGlobalAdmin(User user) {
        return this.sysAdmins.contains(user.getEmail());
    }

    private Publisher<Boolean> hasAccess(User u, Calendar calendar) {
        return Mono.just(isGlobalAdmin(u))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.just(calendar.getOwner().equals(u.getId()))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(groupRepo
                                .findById(calendar.getOwner())
                                .map(Group::getId)
                                .flatMapMany(memberRepo::getAllByGid)
                                .map(Member::getUid)
                                .map(uuid -> uuid.equals(calendar.getOwner()))
                                .filter(Boolean::booleanValue)
                                .next()));
    }

    private Publisher<Boolean> hasAccess(User u, TodoList todoList) {
        return Mono.just(isGlobalAdmin(u))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.just(todoList.getOwner().equals(u.getId()))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(memberRepo
                                .getByUidAndGid(u.getId(), todoList.getId())
                                .thenReturn(true)));
    }

    private Mono<User> user(Mono<Authentication> principal) {
        return principal.map(Authentication::getPrincipal).cast(User.class);
    }

    //endregion
}
