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

package at.tgm.schulplaner.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-09
 */
@Slf4j
@Configuration
public class MySqlConfig extends AbstractR2dbcConfiguration {

    private @Value("spring.r2dbc.url") String url;
    private @Value("spring.r2dbc.username") String username;
    private @Value("spring.r2dbc.password") String password;

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions
                .parse(url)
                .mutate()
                .option(ConnectionFactoryOptions.USER, username)
                .option(ConnectionFactoryOptions.PASSWORD, password)
                .build());
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(UuidToStringConverter.INSTANCE);
        converterList.add(StringToUuidConverter.INSTANCE);
        return new R2dbcCustomConversions(getStoreConversions(), converterList);
    }
    @WritingConverter
    enum UuidToStringConverter implements Converter<UUID, String> {
        INSTANCE;

        @Override
        public String convert(UUID uuid) {
            return uuid.toString();
        }
    }

    @ReadingConverter
    enum StringToUuidConverter implements Converter<String, UUID> {
        INSTANCE;

        @Override
        public UUID convert(String value) {
            return UUID.fromString(value);
        }
    }
}
