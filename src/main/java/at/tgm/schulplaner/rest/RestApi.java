package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.dto.UserDTO;
import at.tgm.schulplaner.model.Group;
import at.tgm.schulplaner.model.Member;
import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.repository.GroupRepository;
import at.tgm.schulplaner.repository.MemberRepository;
import at.tgm.schulplaner.repository.UserRepository;
import at.tgm.schulplaner.security.JWTUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
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
public class RestApi {
    //region repositories

    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider;
    private final Collection<String> globalAdmins;
    private final JWTUtil jwtUtil;

    public RestApi(UserRepository userRepo,
                   MemberRepository memberRepo,
                   GroupRepository groupRepo,
                   JWTUtil jwtUtil,
                   ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider,
                   @Value("${admin_accounts}") Collection<String> globalAdmins) {
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.groupRepo = groupRepo;
        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
        this.globalAdmins = globalAdmins;
        this.jwtUtil = jwtUtil;
    }

    //endregion

    //region global admin

    @GetMapping("/users")
    public Flux<UserDTO> getUsers(Mono<Authentication> principal) {
        return ifGlobalAdmin(user(principal))
                .flatMapMany(v -> userRepo.findAll())
                .map(UserDTO::new);
    }

    @GetMapping("/user/{uid}")
    public Mono<UserDTO> getUser(Mono<Authentication> principal, @PathVariable String uid) {
        return ifGlobalAdmin(user(principal))
                .flatMap(v -> userRepo.findById(UUID.fromString(uid)))
                .map(UserDTO::new);
    }

    @GetMapping("/user/{uid}/groups")
    public Flux<Group> getUserGroups(Mono<Authentication> principal, @PathVariable String uid) {
        return ifGlobalAdmin(user(principal))
                .map(v -> UUID.fromString(uid))
                .flatMap(userRepo::findById)
                .flatMap(user -> {
                    UUID id;
                    if ((id = user.getId()) != null) {
                        return Mono.just(id);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMapMany(memberRepo::getAllByUid)
                .map(Member::getGid)
                .flatMap(groupRepo::findById);
    }

    //endregion

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    static class AuthRequest {
        private String username;
        private String password;
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    static class AuthResponse {
        private String token;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody Mono<AuthRequest> request) {
        return request.map(authRequest ->
                ResponseEntity.ok(
                        new AuthResponse(
                                jwtUtil.generateToken(
                                        (User) ldapAuthenticationProvider.authenticate(
                                                new UsernamePasswordAuthenticationToken(
                                                        authRequest.getUsername(),
                                                        authRequest.getPassword()))
                                                .getPrincipal()))))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/user")
    public Mono<UserDTO> getUserInfo(Mono<Authentication> principal) {
        return user(principal).map(UserDTO::new);
    }

    @GetMapping("/groups")
    public Flux<Group> getGroups(Mono<Authentication> principal) {
        return user(principal)
                .flatMapMany(user -> groupRepo
                        .findAll()
                        .map(group -> Pair.of(user, group)))
                .filterWhen(pair -> {
                    User user = pair.getFirst();
                    Group group = pair.getSecond();
                    return memberRepo
                            .getByUidAndGid(user.getId(), group.getId())
                            .map(member -> true)
                            .defaultIfEmpty(isGlobalAdmin(user));
                })
                .map(Pair::getSecond);
    }

    @GetMapping("/group/{gid}")
    public Mono<Group> getGroup(Mono<Authentication> principal, @PathVariable String gid) {
        return user(principal)
                .filterWhen(user -> memberRepo
                        .getByUidAndGid(user.getId(), UUID.fromString(gid))
                        .map(member -> true)
                        .defaultIfEmpty(isGlobalAdmin(user)))
                .map(user -> UUID.fromString(gid))
                .flatMap(groupRepo::findById);
    }

    @GetMapping("/group/{gid}/users")
    public Flux<UserDTO> getGroupUsers(Mono<Authentication> principal, @PathVariable String gid) {
        return user(principal)
                .filterWhen(user -> memberRepo
                        .getByUidAndGid(user.getId(), UUID.fromString(gid))
                        .map(member -> true)
                        .defaultIfEmpty(isGlobalAdmin(user)))
                .map(s -> UUID.fromString(gid))
                .flatMap(groupRepo::findById)
                .flatMap(group -> {
                    UUID id;
                    if ((id = group.getId()) != null) {
                        return Mono.just(id);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMapMany(memberRepo::getAllByUid)
                .map(Member::getUid)
                .flatMap(userRepo::findById)
                .map(UserDTO::new);
    }

    //region helpers

    private Mono<User> ifGlobalAdmin(Mono<User> user) {
        return user.filter(this::isGlobalAdmin);
    }

    private boolean isGlobalAdmin(User user) {
        return this.globalAdmins.contains(user.getEmail());
    }

    private Mono<User> user(Mono<Authentication> principal) {
        return principal.map(Authentication::getPrincipal).cast(User.class);
    }

    //endregion
}
