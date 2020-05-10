package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.dto.UserDTO;
import at.tgm.schulplaner.model.Member;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
public class UserApi {
    private final DataManager dataManager;

    @Operation(tags = {"user"}, summary = "Get all users filtered by q and limited by size and page if the user is authenticated", security =
    @SecurityRequirement(name= "BEARER KEY"), parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "q"),
            @Parameter(in = ParameterIn.QUERY, name = "page"),
            @Parameter(in = ParameterIn.QUERY, name = "size")})
    @GetMapping("/users")
    public Flux<UserDTO> searchForUser(@AuthenticationPrincipal Mono<User> principal,
                                       @RequestParam(value = "q", required = false, defaultValue = "") @Parameter(hidden = true) String q,
                                       @Parameter(hidden = true) Pageable pageable) {
        return principal
                .flatMapMany(p -> dataManager.getUserRepo().findAllByEmailContainsIgnoreCaseOrNameContainsIgnoreCase(q, q, pageable)
                        .map(UserDTO::new));
    }

    @Operation(tags = {"user"}, summary = "Get the authenticated user", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/user")
    public Mono<UserDTO> getUserInfo(@AuthenticationPrincipal Mono<User> principal) {
        return principal.map(UserDTO::new);
    }


    @Operation(tags = {"group", "user"}, summary = "Get all users that are a member of the group if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}/users")
    public Flux<UserDTO> getGroupUsers(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return dataManager.filterGroupID(principal, gid)
                .flatMapMany(dataManager.getMemberRepo()::getAllByGid)
                .map(Member::getUid)
                .flatMap(dataManager.getUserRepo()::findById)
                .map(UserDTO::new);
    }
}
