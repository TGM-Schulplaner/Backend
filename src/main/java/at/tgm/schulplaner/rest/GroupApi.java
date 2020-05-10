package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.Group;
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
                        .filterWhen(group -> dataManager.hasAccess(user, group)));
    }

    @Operation(tags = {"group"}, summary = "Get a group by its id if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}")
    public Mono<Group> getGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return dataManager.getGroup(principal, gid);
    }
}
