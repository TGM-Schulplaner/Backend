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
@Table("todo_list")
public class TodoList {

    private @Id @Nullable UUID id;
    private final UUID owner;
    private @NonNull String title;

    @PersistenceConstructor
    public TodoList(@Nullable UUID id, @NonNull UUID owner, @NonNull String title) {
        Assert.notNull(owner, "Owner must not be null!");
        Assert.hasText(title, "Title must not be empty!");
        this.id = id;
        this.owner = owner;
        this.title = title;
    }

    public TodoList(@NonNull UUID owner, @NonNull String title) {
        this(null, owner, title);
    }
}
