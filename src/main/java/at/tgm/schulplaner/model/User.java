package at.tgm.schulplaner.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Georg Burkl
 * @version 2020-03-11
 */
@Data
@ToString
@Table("user")
@EqualsAndHashCode
@RequiredArgsConstructor
public class User {

    @Id private Long id;
    private final String username;

}
