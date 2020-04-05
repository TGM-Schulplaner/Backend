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

import java.io.Serializable;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
@Table("todo_item")
public class TodoItem {

    private @Id @Nullable UUID id;
    private final UUID list;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull Status status;

    @PersistenceConstructor
    public TodoItem(@Nullable UUID id, @NonNull UUID list, @NonNull String name, @NonNull String description, @NonNull Status status) {
        Assert.notNull(list, "TodoList must not be null!");
        Assert.hasText(name, "Name must not be empty!");
        Assert.notNull(description, "Description must not be null (but may be empty)!");
        this.id = id;
        this.list = list;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public TodoItem(@NonNull UUID list, @NonNull String name, @NonNull String description, @NonNull Status status) {
        this(null, list, name, description, status);
    }

    public enum Status implements Serializable { UNSTARTED, IN_PROGRESS, DONE, FAILED }
}
