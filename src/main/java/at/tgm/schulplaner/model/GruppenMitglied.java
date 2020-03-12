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
public class GruppenMitglied {
    @Id private UUID id;
    private final UUID groupID;
    private final UUID userID;
    private boolean isAdmin = false;
}
