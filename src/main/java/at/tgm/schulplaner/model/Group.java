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

import at.tgm.schulplaner.rest.DataManager;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Table("`group`")
public class Group {
    private static final String DEFAULT_SETTINGS = Arrays.stream(DataManager.AccessEntity.values()).map(Enum::name).map(s -> s+DataManager.AccessType.READ.name()).collect(Collectors.joining(","));
    private @Id @Nullable UUID id;
    private @NonNull String name;
    private @NonNull String description;
    private @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) @NonNull String settings;

    @PersistenceConstructor
    public Group(@Nullable UUID id, @NonNull String name, @NonNull String description, @Nullable String settings) {
        Assert.hasText(name, "Name must not be empty!");
        Assert.notNull(description, "Description must not be null (but may be empty)!");
        this.id = id;
        this.name = name;
        this.description = description;
        this.settings = settings != null ? settings : DEFAULT_SETTINGS;
    }

    public Group(@NonNull String name, @NonNull String description) {
        this(null, name, description, null);
    }

    public boolean checkPermission(DataManager.AccessEntity accessEntity, DataManager.AccessType accessType) {
        if (accessType == DataManager.AccessType.READ && accessEntity == DataManager.AccessEntity.GROUP) {
            return true;
        }
        return settings.contains(accessEntity.name()+"="+accessType.name());
    }
    
    public void setPermission(DataManager.AccessEntity accessEntity,DataManager.AccessType accessType) {
        this.settings = Arrays.stream(settings.split(","))
                .map(s -> s.split("="))
                .map(strings -> {
                    if (strings[0].equals(accessEntity.name())) {
                        strings[1] = accessType.name();
                    }
                    return String.join("=", strings);
                })
                .collect(Collectors.joining(","));
    }

    @Value
    public static class NewGroup {
        @NonNull @NotBlank String name;
        @NonNull String description;

        public Group asGroup() {
            return new Group(this.name, this.description);
        }
    }

    @Value
    public static class ModifyGroup {
        @Nullable String name;
        @Nullable String description;
        @Nullable Map<DataManager.AccessEntity, DataManager.AccessType> settings;

        public Group modify(Group group) {
            if (name != null && !name.equals(group.name)) {
                group.setName(name);
            }
            if (description != null && !description.equals(group.description)) {
                group.setDescription(description);
            }
            if (settings != null) {
                settings.forEach(group::setPermission);
            }
            return group;
        }
    }
}
