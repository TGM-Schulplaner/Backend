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

package at.tgm.schulplaner.security.jwt;

import at.tgm.schulplaner.model.User;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Georg Burkl
 * @version 2020-04-05
 */
@Component
public class JWTUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JWTToken.Generator generator;

    public JWTUtil(JWTProperties properties) {
        this.generator = new JWTToken.Generator(properties.getSecret(), properties.getExpiration());
    }

    public JWTToken generateToken(User user) {
        return this.generator.generateToken(user);
    }

    public JWTToken parseToken(String token) {
        return this.generator.parseToken(token);
    }
}
