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

import at.tgm.schulplaner.config.ConfigProperties;
import at.tgm.schulplaner.security.jwt.JWTAuthenticationManager;
import at.tgm.schulplaner.security.jwt.JWTAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Slf4j
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final JWTAuthenticationManager jwtAuthManager;
    private final AntPathMatcher antMatcher;
    private final ConfigProperties properties;

    public SecurityContextRepository(JWTAuthenticationManager jwtAuthManager, ConfigProperties properties) {
        this.jwtAuthManager = jwtAuthManager;
        this.properties = properties;
        antMatcher = new AntPathMatcher();
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
            }
        }
        String path = swe.getRequest().getPath().pathWithinApplication().value();
        if (properties.getIgnoredPaths().stream().anyMatch(pattern -> antMatcher.match(pattern, path)) || ("/api/v1/push".equals(path) && swe.getRequest().getMethod() == HttpMethod.GET)) {
            return Mono.empty();
        }
        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        swe.getResponse().bufferFactory().allocateBuffer().write("Unauthorized", Charset.defaultCharset());
        return Mono.empty(/*WebClientResponseException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", new HttpHeaders(), new byte[0], null)*/);
    }

}
