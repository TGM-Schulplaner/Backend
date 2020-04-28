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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;


/**
 * @author Georg Burkl
 * @version 2020-04-11
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1")
public class PushApi {
    private final FluxProcessor<String, String> pushPipe = DirectProcessor.<String>create().serialize();
    private final FluxSink<String> sink = pushPipe.sink();

    @GetMapping("/push")
    public Flux<ServerSentEvent<String>> getPush() {
        return pushPipe.map(o -> ServerSentEvent.builder(o).build());
    }

    @PostMapping("/push")
    public void push(@RequestBody String body, Authentication authentication) {
        if (authentication.getPrincipal().equals("PUSH") && authentication.isAuthenticated()) sink.next(body);
    }
}
