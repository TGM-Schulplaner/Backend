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

package at.tgm.schulplaner.security;

import at.tgm.schulplaner.security.jwt.JWTAuthenticationManager;
import at.tgm.schulplaner.security.jwt.JWTAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Slf4j
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String PUSH_TOKEN_KEY = "PushToken ";
    private @Value("${push_service.token}") String pushToken;
    private final JWTAuthenticationManager jwtAuthManager;

    public SecurityContextRepository(JWTAuthenticationManager jwtAuthManager) {
        this.jwtAuthManager = jwtAuthManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null) {
            if (authHeader.startsWith(TOKEN_PREFIX)) {
//            log.info(request.getLocalAddress().getHostName());
                log.info("{} {}?{}",
                        request.getMethodValue(),
                        request.getPath().value(),
                        request.getQueryParams()
                                .entrySet()
                                .stream()
                                .flatMap(entry -> entry
                                        .getValue()
                                        .stream()
                                        .map(val -> entry.getKey() + "=" + val))
                                .collect(Collectors.joining("&")));
                String authToken = authHeader.substring(TOKEN_PREFIX.length());
                Authentication auth = new JWTAuthenticationToken(authToken);
                return this.jwtAuthManager.authenticate(auth).map(SecurityContextImpl::new);
            } else if (authHeader.startsWith(PUSH_TOKEN_KEY) && authHeader.substring(PUSH_TOKEN_KEY.length()).equals(pushToken)) {
                return Mono.just(new UsernamePasswordAuthenticationToken("PUSH", authHeader.substring(PUSH_TOKEN_KEY.length()), null)).map(SecurityContextImpl::new);
            }
        }
        List<PathContainer.Element> elements = swe.getRequest().getPath().pathWithinApplication().elements();
        String value = elements.get(elements.size() - 1).value();
        if (value.equals("login") || (value.equals("push"))) {
            return Mono.empty();
        }
        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        swe.getResponse().bufferFactory().allocateBuffer().write("Unauthorized", Charset.defaultCharset());
        return Mono.empty(/*WebClientResponseException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", new HttpHeaders(), new byte[0], null)*/);
    }

}
