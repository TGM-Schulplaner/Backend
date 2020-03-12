package at.tgm.schulplaner.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

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
public class Gruppe {
    @Id private UUID id;
    private @NonNull String name;
    private @NonNull String beschreibung;
}
