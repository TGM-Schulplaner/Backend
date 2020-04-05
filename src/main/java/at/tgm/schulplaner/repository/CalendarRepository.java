package at.tgm.schulplaner.repository;

import at.tgm.schulplaner.model.Calendar;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CalendarRepository extends ReactiveCrudRepository<Calendar, UUID> {
}
