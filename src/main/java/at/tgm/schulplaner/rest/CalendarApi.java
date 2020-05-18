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

import at.tgm.schulplaner.model.Calendar;
import at.tgm.schulplaner.model.CalendarEntry;
import at.tgm.schulplaner.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
@Slf4j
@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CalendarApi {
    private final DataManager dataManager;

    @Operation(tags = {"user", "calendar"}, summary = "Get all calendars that belong to the authenticated user", security = @SecurityRequirement(name= "bearer-key"))
    @GetMapping("/user/calendars")
    public Flux<Calendar> getCalendarsForUser(@AuthenticationPrincipal Mono<User> principal) {
        return principal.flatMapMany(u -> dataManager.getCalendarRepo().findAllByOwner(u.getId()));
    }

    @Operation(tags = {"user", "calendar"}, summary = "Create new calendar that belongs to the group if the authenticated user has access", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/user/calendar")
    public Mono<Calendar> createCalendarForUser(@AuthenticationPrincipal Mono<User> principal, @RequestBody Calendar.NewCalendar calendar) {
        return principal.flatMap(user -> dataManager.getCalendarRepo().save(calendar.asCalendar(user.getId())));
    }

    @Operation(tags = {"group", "calendar"}, summary = "Get all calendars that belong to the group if the authenticated user has access", security = @SecurityRequirement(name= "bearer-key"))
    @GetMapping("/group/{gid}/calendars")
    public Flux<Calendar> getCalendarsForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.READ, DataManager.AccessEntity.CALENDAR))
                .thenMany(dataManager.getCalendarRepo().findAllByOwner(gid));
    }

    @Operation(tags = {"group", "calendar"}, summary = "Create new calendar that belongs to the group if the authenticated user has access", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/group/{gid}/calendar")
    public Mono<Calendar> createCalendarForGroup(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID gid, @RequestBody Calendar.NewCalendar calendar) {
        return principal
                .filterWhen(user -> dataManager.hasAccessToGroup(user, gid, DataManager.AccessType.CREATE, DataManager.AccessEntity.CALENDAR))
                .then(dataManager.getCalendarRepo().save(calendar.asCalendar(gid)));
    }

    @Operation(tags = {"calendar"}, summary = "Get calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @GetMapping("/calendar/{id}")
    public Mono<Calendar> getCalendar(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return dataManager.getCalendar(principal, id, DataManager.AccessType.READ, false);
    }

    @Operation(tags = {"calendar"}, summary = "Delete calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/calendar/{id}")
    public Mono<Void> deleteCalendar(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id) {
        return dataManager.getCalendar(principal, id, DataManager.AccessType.DELETE, false).flatMap(dataManager.getCalendarRepo()::delete);
    }

    @Operation(tags = {"calendar"}, summary = "Modify calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @PutMapping("/calendar/{id}")
    public Mono<Calendar> modifyCalendar(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id, @RequestBody Calendar.ModifyCalendar modifyCalendar) {
        return dataManager.getCalendar(principal, id, DataManager.AccessType.MODIFY, false).map(modifyCalendar::modify).flatMap(dataManager.getCalendarRepo()::save);
    }

    @Operation(tags = {"calendar"}, summary = "Get all entries for a calendar by id, filtered by start and end", security = @SecurityRequirement(name= "bearer-key"))
    @GetMapping("/calendar/{id}/entries")
    public Flux<CalendarEntry> getCalendarEntries(@AuthenticationPrincipal Mono<User> principal,
                                                  @PathVariable UUID id,
                                                  @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                  @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return principal.filterWhen(user -> dataManager.hasAccessToCalendar(user, id, DataManager.AccessType.READ, true))
                .thenMany(start != null && end != null ?
                        dataManager.getCalendarEntryRepo().findAllByCalendarAndStartIsLessThanEqualAndEndIsGreaterThanEqual(id, end, start) :
                        dataManager.getCalendarEntryRepo().findAllByCalendar(id));
    }

    @Operation(tags = {"calendar"}, summary = "Create a new entry for a calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/calendar/{id}/entry")
    public Mono<CalendarEntry> createCalendarEntry(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID id, @RequestBody CalendarEntry.NewCalendarEntry newCalendarEntry) {
        return principal.filterWhen(user -> dataManager.hasAccessToCalendar(user, id, DataManager.AccessType.CREATE, true))
                .thenReturn(newCalendarEntry.asCalendarEntry(id))
                .flatMap(dataManager.getCalendarEntryRepo()::save);
    }

    @Operation(tags = {"calendar"}, summary = "Create a new entry for a calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/calendar/{calendarId}/entry/{id}")
    public Mono<Void> deleteCalendarEntry(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID calendarId, @PathVariable UUID id) {
        return principal.filterWhen(user -> dataManager.hasAccessToCalendar(user, calendarId, DataManager.AccessType.DELETE, true))
                .then(dataManager.getCalendarEntryRepo().findById(id))
                .filter(calendarEntry -> calendarEntry.getCalendar().equals(calendarId))
                .flatMap(dataManager.getCalendarEntryRepo()::delete);
    }

    @Operation(tags = {"calendar"}, summary = "Modify an entry for a calendar by id", security = @SecurityRequirement(name= "bearer-key"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/calendar/{calendarId}/entry/{id}")
    public Mono<Void> modifyCalendarEntry(@AuthenticationPrincipal Mono<User> principal, @PathVariable UUID calendarId, @PathVariable UUID id, @RequestBody CalendarEntry.ModifyCalendarEntry newCalendarEntry) {
        return principal.filterWhen(user -> dataManager.hasAccessToCalendar(user, calendarId, DataManager.AccessType.MODIFY, true))
                .then(dataManager.getCalendarEntryRepo().findById(id))
                .filter(calendarEntry -> calendarEntry.getCalendar().equals(calendarId))
                .map(newCalendarEntry::modify)
                .flatMap(dataManager.getCalendarEntryRepo()::save)
                .then();
    }
}
