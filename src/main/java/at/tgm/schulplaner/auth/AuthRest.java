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

package at.tgm.schulplaner.auth;

import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Georg Burkl
 * @version 2020-04-08
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthRest {

    private final ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider;
    private final JWTUtil jwtUtil;

    @PostMapping("/api/v1/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody Mono<AuthRequest> request) {
        return request.map(authRequest ->
                ResponseEntity.ok(
                        new AuthResponse(
                                jwtUtil.generateToken(
                                        (User) ldapAuthenticationProvider.authenticate(
                                                new UsernamePasswordAuthenticationToken(
                                                        authRequest.getUsername(),
                                                        authRequest.getPassword()))
                                                .getPrincipal())
                                        .getTokenString())))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
