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
@Table("\"GROUP\"")
public class Group {
    private @Id @Nullable UUID id;
    private @NonNull String name;
    private @NonNull String description;

    @PersistenceConstructor
    public Group(@Nullable UUID id, @NonNull String name, @NonNull String description) {
        Assert.hasText(name, "Name must not be empty!");
        Assert.notNull(description, "Description must not be null (but may be empty)!");
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Group(@NonNull String name, @NonNull String description) {
        this(null, name, description);
    }
}
