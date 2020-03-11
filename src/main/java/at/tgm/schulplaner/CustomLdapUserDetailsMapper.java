package at.tgm.schulplaner;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyResponseControl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import java.util.Collection;

/**
 * @author Georg Burkl
 * @version 2020-03-11
 */
public class CustomLdapUserDetailsMapper extends LdapUserDetailsMapper {

    private static final String ALLE = "alle";
    private static final String EVERYONE_PASSWORD = "everyonePassword";
    private static final String SCHUELER = "schueler";

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String dn = ctx.getNameInNamespace();

        TGMLdapUserDetails.Essence essence = new TGMLdapUserDetails.Essence();
        essence.setDn(dn);

        Object passwordValue = ctx.getObjectAttribute("userPassword");

        if (passwordValue != null) {
            essence.setPassword(mapPassword(passwordValue));
        }

        essence.setUsername(username);
        essence.setName(ctx.getStringAttribute("name"));
        String employeeType = ctx.getStringAttribute("employeeType");
        essence.setEmployeeType(employeeType);

        if (SCHUELER.equals(employeeType))
            essence.setDepartment(ctx.getStringAttribute("department"));

        // Add the supplied authorities

        for (GrantedAuthority authority : authorities) {
            String a = authority.getAuthority();
            if (a.equals(EVERYONE_PASSWORD) || a.equals(ALLE) || a.startsWith(SCHUELER))
                continue;
            essence.addAuthority(authority);
        }

        // Check for PPolicy data

        PasswordPolicyResponseControl ppolicy = (PasswordPolicyResponseControl) ctx
                .getObjectAttribute(PasswordPolicyControl.OID);

        if (ppolicy != null) {
            essence.setTimeBeforeExpiration(ppolicy.getTimeBeforeExpiration());
            essence.setGraceLoginsRemaining(ppolicy.getGraceLoginsRemaining());
        }

        return essence.createUserDetails();
    }

    /*private Mono<User> getOrCreateUser(String username) {
        return userRepo.getByUsername(username)
                .switchIfEmpty(userRepo.save(new User(username)));
    }*/
}
