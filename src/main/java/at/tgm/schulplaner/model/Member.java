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
