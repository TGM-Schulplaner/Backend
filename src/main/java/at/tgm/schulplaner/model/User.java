package at.tgm.schulplaner.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Georg Burkl
 * @version 2020-03-22
 */
@Data
@ToString
@EqualsAndHashCode
@Table("user")
public class User implements UserDetails {

    private static final Predicate<String> EMAIL_PREDICATE = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$").asMatchPredicate();
    public static final String STUDENT = "schueler";

    private @Id @Nullable UUID id;
    private @NonNull String email;
    private @NonNull String name;
    private @NonNull String type;
    private @Nullable String department;

    @PersistenceConstructor
    public User(@Nullable UUID id, @NonNull String email, @NonNull String name, @NonNull String type, @Nullable String department) {
        Assert.hasText(email, "Email must not be empty!");
        Assert.isTrue(EMAIL_PREDICATE.test(email), "Email must be a valid email address!");
        Assert.hasText(name, "Name must not be empty!");
        Assert.notNull(type, "Employee type must not be null (but may be empty)!");
        if (Objects.equals(STUDENT, type)) {
            Assert.hasText(department, "Department must not be empty on student type user entry!");
        }
        this.id = id;
        this.email = email;
        this.name = name;
        this.type = type;
        this.department = department;
    }

    public User(@NonNull String email, @NonNull String name, @NonNull String type, @Nullable String department) {
        this(null, email, name, type, department);
    }

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Returns the username used to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
