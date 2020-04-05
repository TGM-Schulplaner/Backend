package at.tgm.schulplaner.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Table("calendar_entry")
public class CalendarEntry {

    private @Id @Nullable UUID id;
    private final UUID calendar;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull LocalDateTime begin;
    private @NonNull LocalDateTime end;

    @PersistenceConstructor
    public CalendarEntry(@Nullable UUID id, @NonNull UUID calendar, @NonNull String name, @NonNull String description, @NonNull LocalDateTime begin, @NonNull LocalDateTime end) {
        Assert.notNull(calendar, "Calendar id must not be null!");
        Assert.hasText(name, "Name must not be empty!");
        Assert.notNull(description, "Description must not be null (but may be empty)!");
        Assert.notNull(begin, "Begin date time must not be null!");
        Assert.notNull(end, "End date time must not be null!");
        this.id = id;
        this.calendar = calendar;
        this.name = name;
        this.description = description;
        this.begin = begin;
        this.end = end;
    }

    public CalendarEntry(@NonNull UUID calendar, @NonNull String name, @NonNull String description, @NonNull LocalDateTime begin, @NonNull LocalDateTime end) {
        this(null, calendar, name, description, begin, end);
    }
}
