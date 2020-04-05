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

import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Table("calendar")
public class Calendar {

    private @Id @Nullable UUID id;
    private final UUID owner;
    private @NonNull String name;

    @PersistenceConstructor
    public Calendar(@Nullable UUID id, @NonNull UUID owner, @NonNull String name) {
        Assert.notNull(owner, "Owner id must not be null!");
        Assert.notNull(name, "Name must not be null!");
        this.id = id;
        this.owner = owner;
        this.name = name;
    }

    public Calendar(@NonNull UUID owner, @NonNull String name) {
        this(null, owner, name);
    }
}
