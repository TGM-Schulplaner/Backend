package at.tgm.schulplaner.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Georg Burkl
 * @version 2020-04-08
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
class AuthRequest {
    private String username;
    private String password;
}
