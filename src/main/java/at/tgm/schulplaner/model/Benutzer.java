package at.tgm.schulplaner.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import reactor.util.annotation.Nullable;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-03-12
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
@RequiredArgsConstructor
public class Benutzer {
    @Id private UUID id;
    private @NonNull String name;
    private final String email;
    private final String employeeType;
    private @Nullable String klasse;

    public Benutzer(@NonNull String name, String email, String employeeType, @Nullable String klasse) {
        this.name = name;
        this.email = email;
        this.employeeType = employeeType;
        this.klasse = klasse;
    }
}
