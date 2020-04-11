package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.dto.UserDTO;
import at.tgm.schulplaner.model.Group;
import at.tgm.schulplaner.model.Member;
import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.repository.GroupRepository;
import at.tgm.schulplaner.repository.MemberRepository;
import at.tgm.schulplaner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-03-30
 */
@SuppressWarnings("ConstantConditions")
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1")
//@RequiredArgsConstructor
public class RestApi {
    //region repositories

    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private @Value("${admin_accounts}") Collection<String> sysAdmins;

    public RestApi(UserRepository userRepo, MemberRepository memberRepo, GroupRepository groupRepo) {
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.groupRepo = groupRepo;
    }

    //endregion

    @GetMapping("/user")
    public Mono<UserDTO> getUserInfo(Mono<Authentication> principal) {
        return user(principal).map(UserDTO::new);
    }

    @GetMapping("/groups")
    public Flux<Group> getGroups(Mono<Authentication> principal) {
        return user(principal).flatMapMany(user -> groupRepo.findAll().filter(group -> memberRepo.existsByUidAndGid(user.getId(), group.getId())));
    }

    @GetMapping("/group/{gid}")
    public Mono<Group> getGroup(Mono<Authentication> principal, @PathVariable String gid) {
        return user(principal)
                .flatMap(user -> memberRepo.getByUidAndGid(user.getId(), UUID.fromString(gid)))
                .map(Member::getGid)
                .flatMap(groupRepo::findById);
    }

    @GetMapping("/group/{gid}/users")
    public Flux<UserDTO> getGroupUsers(Mono<Authentication> principal, @PathVariable String gid) {
        return user(principal)
                .flatMap(user -> memberRepo.getByUidAndGid(user.getId(), UUID.fromString(gid)))
                .map(Member::getGid)
                .flatMapMany(memberRepo::getAllByGid)
                .map(Member::getUid)
                .flatMap(userRepo::findById)
                .map(UserDTO::new);
    }

    @GetMapping("/users")
    public Flux<UserDTO> searchForUser(@RequestParam(value = "q",    required = false, defaultValue = "") String q,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                       @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return userRepo.findAllByEmailContainsIgnoreCaseOrNameContainsIgnoreCase(q, q, PageRequest.of(page, size)).map(UserDTO::new);
    }

    //region helpers

    private Mono<User> ifGlobalAdmin(Mono<User> user) {
        return user.filter(this::isGlobalAdmin);
    }

    private boolean isGlobalAdmin(User user) {
        return this.sysAdmins.contains(user.getEmail());
    }

    private Mono<User> user(Mono<Authentication> principal) {
        return principal.map(Authentication::getPrincipal).cast(User.class);
    }

    //endregion
}
