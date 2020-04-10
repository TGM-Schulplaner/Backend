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
@Table("member")
public class Member {

    private @Id @Nullable UUID id;
    private final @NonNull UUID uid;
    private final @NonNull UUID gid;
    private boolean isAdmin;

    @PersistenceConstructor
    public Member(@Nullable UUID id, @NonNull UUID uid, @NonNull UUID gid, boolean isAdmin) {
        Assert.notNull(uid, "User id must not be null!");
        Assert.notNull(gid, "Group id must not be null!");
        this.id = id;
        this.uid = uid;
        this.gid = gid;
        this.isAdmin = isAdmin;
    }

    public Member(@NonNull UUID uid, @NonNull UUID gid, boolean isAdmin) {
        this(null, uid, gid, isAdmin);
    }

    public Member(@NonNull UUID uid, @NonNull UUID gid) {
        this(null, uid, gid, false);
    }
}
