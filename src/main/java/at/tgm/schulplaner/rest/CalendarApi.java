package at.tgm.schulplaner.rest;

import at.tgm.schulplaner.model.Calendar;
import at.tgm.schulplaner.model.CalendarEntry;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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
public class CalendarApi {
    private final DataManager dataManager;

    @Operation(tags = {"user", "calendar"}, summary = "Get all calendars that belong to the authenticated user", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/user/calendars")
    public Flux<Calendar> getCalendarsForUser(@AuthenticationPrincipal Mono<User> principal) {
        return principal.flatMapMany(u -> dataManager.getCalendarRepo().findAllByOwner(u.getId()));
    }

    @Operation(tags = {"group", "calendar"}, summary = "Get all calendars that belong to the group if the authenticated user has access", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/group/{gid}/calendars")
    public Flux<Calendar> getCalendarsForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return dataManager.filterGroupID(principal, gid).flatMapMany(dataManager.getCalendarRepo()::findAllByOwner);
    }

    @Operation(tags = {"calendar"}, summary = "Get calendar by id", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/calendar/{id}")
    public Mono<Calendar> getCalendar(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return principal
                .flatMap(u -> dataManager.getCalendarRepo()
                        .findById(id)
                        .filterWhen(calendar -> dataManager.hasAccess(u, calendar)));
    }

    @Operation(tags = {"calendar"}, summary = "Get all entries for a calendar by id, filtered by start and end", security = @SecurityRequirement(name= "BEARER KEY"))
    @GetMapping("/calendar/{id}/entries")
    public Flux<CalendarEntry> getCalendarEntries(@AuthenticationPrincipal Mono<User> principal,
                                                  @PathVariable UUID id,
                                                  @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                  @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Mono<UUID> calendarId = getCalendar(principal, id).map(Calendar::getId);
        if (start != null && end != null) {
            return calendarId.flatMapMany(c -> dataManager.getCalendarEntryRepository()
                    .findAllByCalendarAndStartIsLessThanEqualAndEndIsGreaterThanEqual(c, end, start));
        }
        return calendarId.flatMapMany(dataManager.getCalendarEntryRepository()::findAllByCalendar);
    }
}
