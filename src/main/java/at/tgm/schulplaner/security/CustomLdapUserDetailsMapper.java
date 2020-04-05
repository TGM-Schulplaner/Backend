package at.tgm.schulplaner.security;

import at.tgm.schulplaner.model.User;
import at.tgm.schulplaner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Georg Burkl
 * @version 2020-03-11
 */
@RequiredArgsConstructor
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper {

    private final UserRepository userRepo;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String name = ctx.getStringAttribute("name");
        String employeeType = ctx.getStringAttribute("employeeType");
        String department = ctx.getStringAttribute("department");

        return getOrCreateUser(username, name, employeeType, department).block();
    }

    private Mono<User> getOrCreateUser(@NonNull String username, String name, String type, String department) {
        return userRepo.findByEmail(username)
                .flatMap(user -> {
                    boolean changed = false;
                    if (!Objects.equals(user.getName(), name)) {
                        user.setName(name);
                        changed = true;
                    }
                    if (!Objects.equals(user.getType(), type)) {
                        user.setType(type);
                        changed = true;
                    }
                    if (!Objects.equals(user.getDepartment(), department)) {
                        user.setDepartment(department);
                        changed = true;
                    }
                    if (changed) {
                        return userRepo.save(user);
                    }
                    return Mono.just(user);
                }).switchIfEmpty(userRepo.save(new User(username, name, type, department)));
    }
}
