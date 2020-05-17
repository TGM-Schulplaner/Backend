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

import at.tgm.schulplaner.config.ConfigProperties;
import at.tgm.schulplaner.model.Calendar;
import at.tgm.schulplaner.model.Group;
import at.tgm.schulplaner.model.TodoList;
import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@SuppressWarnings({"ConstantConditions"})
@Data
@Component
@RequiredArgsConstructor
public class DataManager {
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final CalendarRepository calendarRepo;
    private final CalendarEntryRepository calendarEntryRepo;
    private final TodoListRepository todoListRepo;
    private final TodoItemRepository todoItemRepo;
    private final ConfigProperties properties;

    boolean isGlobalAdmin(User user) {
        return this.properties.getAdminAccounts().contains(user.getEmail());
    }

    public Mono<Boolean> hasAccessToGroup(User user, UUID gid, AccessType accessType, AccessEntity accessEntity) {
        return Mono.just(isGlobalAdmin(user))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(groupRepo.findById(gid)
                        .flatMap(group -> memberRepo
                                .getByUidAndGid(user.getId(), group.getId())
                                .filter(member -> member.isAdmin() || group.checkPermission(accessEntity, accessType))
                                .thenReturn(true)));
    }

    public Mono<Boolean> hasAccessToGroup(User user, Group group, AccessType accessType, AccessEntity accessEntity) {
        return hasAccessToGroup(user, group.getId(), accessType, accessEntity);
    }

    public Mono<Group> getGroup(Mono<User> user, UUID gid, AccessType accessType) {
        return user.flatMap(u -> groupRepo.findById(gid).filterWhen(group -> hasAccessToGroup(u, group, accessType, AccessEntity.GROUP)));
    }

    public Mono<Boolean> hasAccessToCalendar(User user, UUID owner, AccessType accessType, boolean entry) {
        return Mono.just(isGlobalAdmin(user) || owner.equals(user.getId()))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(hasAccessToGroup(user, owner, accessType, entry ? AccessEntity.CALENDAR_ENTRY : AccessEntity.CALENDAR));
    }

    public Mono<Boolean> hasAccessToCalendar(User user, Calendar calendar, AccessType accessType, boolean entry) {
        return hasAccessToCalendar(user, calendar.getOwner(), accessType, entry);
    }

    public Mono<Calendar> getCalendar(Mono<User> user, UUID id, AccessType accessType, boolean entry) {
        return user.flatMap(u -> calendarRepo.findById(id).filterWhen(calendar -> hasAccessToCalendar(u, calendar, accessType, entry)));
    }

    public Mono<Boolean> hasAccessToTodoList(User user, UUID owner, AccessType accessType, boolean item) {
        return Mono.just(isGlobalAdmin(user) || owner.equals(user.getId()))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(hasAccessToGroup(user, owner, accessType, item ? AccessEntity.TODO_ITEM : AccessEntity.TODO_LIST));
    }

    public Mono<Boolean> hasAccessToTodoList(User user, TodoList todoList, AccessType accessType, boolean item) {
        return hasAccessToTodoList(user, todoList.getOwner(), accessType, item);
    }

    public Mono<TodoList> getTodoList(Mono<User> user, UUID id, AccessType accessType, boolean item) {
        return user.flatMap(u -> todoListRepo.findById(id).filterWhen(todoList -> hasAccessToTodoList(u, todoList, accessType, item)));
    }

    public enum AccessType {
        READ, CREATE, MODIFY, DELETE
    }

    public enum AccessEntity {
        GROUP, CALENDAR, CALENDAR_ENTRY, TODO_LIST, TODO_ITEM
    }
}
