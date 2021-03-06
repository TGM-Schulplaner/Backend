/*
 * Copyright (c) 2020 tgm - Die Schule der Technik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tgm.schulplaner.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Georg Burkl
 * @version 2020-03-22
 */
@Data
@Table("user")
public class User implements UserDetailsWrapper {

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
     * Returns the username used to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() {
        return email;
    }

}
