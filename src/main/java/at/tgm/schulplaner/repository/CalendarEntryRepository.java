package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.CalendarEntry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CalendarEntryRepository extends ReactiveCrudRepository<CalendarEntry, UUID> {
}
