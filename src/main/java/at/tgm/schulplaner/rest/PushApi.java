package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PushApi {
    private final DataManager dataManager;
    private final FluxProcessor<String, String> pushPipe = DirectProcessor.<String>create().serialize();
    private final FluxSink<String> sink = pushPipe.sink();

    @Operation(tags = {"push"}, summary = "SSE endpoint for the push event service")
    @GetMapping("/push")
    public Flux<ServerSentEvent<String>> getPush() {
        return pushPipe.map(o -> ServerSentEvent.builder(o).build());
    }

    @Operation(tags = {"push"}, summary = "Send endpoint for the push event service", security = {@SecurityRequirement(name = "BEARER KEY")})
    @PostMapping("/push")
    public Mono<Void> push(@RequestBody String body, @AuthenticationPrincipal Mono<User> authentication) {
        return authentication.filter(dataManager::isGlobalAdmin)
                .then(Mono.fromRunnable(() -> sink.next(body)))
                .then();
    }
}
