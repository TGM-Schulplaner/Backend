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
import at.tgm.schulplaner.model.Group;
import at.tgm.schulplaner.model.Member;
import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.repository.GroupRepository;
import at.tgm.schulplaner.repository.MemberRepository;
import at.tgm.schulplaner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
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
    public Flux<UserDTO> searchForUser(@RequestParam(value = "q", required = false, defaultValue = "") String q, Pageable pageable) {
        return userRepo.findAllByEmailContainsIgnoreCaseOrNameContainsIgnoreCase(q, q, pageable).map(UserDTO::new);
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
