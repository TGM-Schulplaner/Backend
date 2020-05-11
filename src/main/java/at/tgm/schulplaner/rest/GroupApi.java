package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.Group;
import at.tgm.schulplaner.model.Member;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
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
public class GroupApi {
    private final DataManager dataManager;

    @Operation(tags = {"group"}, summary = "Get all groups the authenticated user is a member of", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/groups")
    public Flux<Group> getGroups(@AuthenticationPrincipal Mono<User> principal) {
        return principal
                .flatMapMany(user -> dataManager
                        .getGroupRepo()
                        .findAll()
                        .filterWhen(group -> dataManager.hasAccessToGroup(user, group, DataManager.AccessType.READ, DataManager.AccessEntity.GROUP)));
    }

    @Operation(tags = {"group"}, summary = "Get a group by its id if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}")
    public Mono<Group> getGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return dataManager.getGroup(principal, gid, DataManager.AccessType.READ);
    }

    @SuppressWarnings("ConstantConditions")
    @Operation(tags = {"group"}, summary = "Create a group", security = @SecurityRequirement(name= "BEARER KEY"))
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/group")
    public Mono<Group> createGroup(@AuthenticationPrincipal Mono<User> principal, @RequestBody Group.NewGroup newGroup) {
        return principal.flatMap(user -> dataManager
                .getGroupRepo()
                .save(newGroup.asGroup())
                .map(group -> {
                    dataManager.getMemberRepo().save(new Member(user.getId(), group.getId(), true));
                    return group;
                }));
    }

    @Operation(tags = {"group"}, summary = "Modify a group by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @PutMapping("/group/{gid}")
    public Mono<Group> modifyGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @RequestBody Group.ModifyGroup newGroup) {
        return dataManager
                .getGroup(principal, gid, DataManager.AccessType.MODIFY)
                .map(newGroup::modify)
                .flatMap(dataManager.getGroupRepo()::save);
    }

    @Operation(tags = {"group"}, summary = "Modify a group member for a group", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/group/{gid}/member/{uid}")
    public Mono<Void> modifyMember(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @PathVariable UUID uid, @RequestBody Member.ModifyMember modifyMember) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.MODIFY, DataManager.AccessEntity.GROUP))
                .filter(user -> !Objects.equals(user.getId(), uid))
                .then(dataManager
                        .getMemberRepo()
                        .getByUidAndGid(uid, gid)
                        .map(modifyMember::modify)
                        .flatMap(dataManager.getMemberRepo()::save)).then();
    }

    @Operation(tags = {"group"}, summary = "Modify a group member for a group", security = @SecurityRequirement(name= "BEARER KEY"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/group/{gid}/member")
    public Mono<Void> createMember(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @RequestBody Member.NewMember newMember) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.MODIFY, DataManager.AccessEntity.GROUP))
                .filter(user -> !Objects.equals(user.getId(), newMember.getUid()))
                .thenReturn(newMember.asMember(gid))
                .flatMap(dataManager.getMemberRepo()::save)
                .then();
    }

    @Operation(tags = {"group"}, summary = "Modify a group by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/group/{gid}/member")
    public Mono<Void> modifyMember(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @RequestParam("user") UUID uid) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.MODIFY, DataManager.AccessEntity.GROUP))
                .filter(user -> !Objects.equals(user.getId(), uid))
                .flatMap(user -> dataManager.getMemberRepo().getByUidAndGid(uid, gid))
                .flatMap(dataManager.getMemberRepo()::delete);
    }
}
