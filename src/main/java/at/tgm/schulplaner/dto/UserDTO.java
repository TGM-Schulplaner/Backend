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

package at.tgm.schulplaner.dto;

import at.tgm.schulplaner.model.User;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Georg Burkl
 * @version 2020-03-30
 */
@Slf4j
public class UserDTO {
    private final User user;

    public UserDTO(User user) {
        this.user = user;
    }

    public String getId(){
        return user.getId().toString();
    }
    public String getEmail(){
        return user.getEmail();
    }
    public String getName(){
        return user.getName();
    }
    public String getType(){
        return user.getType();
    }
    public String getDepartment(){
        return user.getDepartment();
    }
}
