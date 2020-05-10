package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.config.ConfigProperties;
import at.tgm.schulplaner.model.*;
import at.tgm.schulplaner.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@SuppressWarnings({"ConstantConditions", "ReactiveStreamsNullableInLambdaInTransform"})
@Data
@Component
@RequiredArgsConstructor
public class DataManager {
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final CalendarRepository calendarRepo;
    private final CalendarEntryRepository calendarEntryRepository;
    private final TodoListRepository todoListRepo;
    private final TodoItemRepository todoItemRepo;
    private final ConfigProperties properties;

    boolean isGlobalAdmin(User user) {
        return this.properties.getAdminAccounts().contains(user.getEmail());
    }

    public Publisher<Boolean> hasAccess(User user, Group group) {
        return Mono.just(isGlobalAdmin(user))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(memberRepo
                        .getByUidAndGid(user.getId(), group.getId())
                        .thenReturn(true));
    }

    public Publisher<Boolean> hasAccess(User user, Calendar calendar) {
        return Mono.just(isGlobalAdmin(user))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.just(calendar.getOwner().equals(user.getId()))
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

    public Publisher<Boolean> hasAccess(User user, TodoList todoList) {
        return Mono.just(isGlobalAdmin(user))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.just(todoList.getOwner().equals(user.getId()))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(memberRepo
                                .getByUidAndGid(user.getId(), todoList.getId())
                                .thenReturn(true)));
    }

    public Mono<Group> getGroup(Mono<User> user, UUID id) {
        return user.flatMap(u -> groupRepo.findById(id)
                .filterWhen(group -> hasAccess(u, group)));
    }

    public Mono<UUID> filterGroupID(Mono<User> user, UUID id) {
        return getGroup(user, id).map(Group::getId);
    }
}
