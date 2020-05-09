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

import at.tgm.schulplaner.repository.UserRepository;
import at.tgm.schulplaner.security.CustomLdapUserDetailsMapper;
import at.tgm.schulplaner.security.SecurityContextRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author Georg Burkl
 * @version 2020-03-05
 */
@Slf4j
@Configuration
@PropertySource("classpath:application_template.yml")
@NoArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${active_directory.url}")    private String adUrl;
    @Value("${active_directory.domain}") private String adDomain;
    @Value("${active_directory.root}")   private String adRoot;
    @Value("${active_directory.filter}") private String adSearchFilter;
    @Value("${secure_endpoints}")        private Collection<String> secureEndpoints;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         SecurityContextRepository securityContextRepository/*,
                                                         CorsConfigurationSource corsConfigurationSource*/) {
        return http
                .exceptionHandling()
                /*.authenticationEntryPoint((swe, e) ->
                        Mono.fromRunnable(() ->
                                swe.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED)))*/
                .accessDeniedHandler((swe, e) ->
                        Mono.fromRunnable(() ->
                                swe.getResponse()
                                        .setStatusCode(HttpStatus.FORBIDDEN)))
                .and()
//                .cors()
                /*.configurationSource(corsConfigurationSource)*/
//                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/v1/login").permitAll()
//                .pathMatchers(secureEndpoints.toArray(String[]::new)).authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    /*@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/

    /*@Bean
    ReactiveAuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider) {
        return new ReactiveAuthenticationManagerAdapter(new ProviderManager(ldapAuthenticationProvider));
    }*/

    @Bean
    ActiveDirectoryLdapAuthenticationProvider ldapAuthenticationProvider(UserRepository repository) {
        ActiveDirectoryLdapAuthenticationProvider adLdap = new ActiveDirectoryLdapAuthenticationProvider(adDomain, adUrl, adRoot);
        if (!adSearchFilter.isBlank())
            adLdap.setSearchFilter(adSearchFilter);
        adLdap.setUserDetailsContextMapper(new CustomLdapUserDetailsMapper(repository));
        return adLdap;
    }

    /**
     * In order to resolve ${...} placeholders in definitions or @Value annotations using properties from
     * a PropertySource, one must register a PropertySourcesPlaceholderConfigurer. This happens
     * automatically when using XML configuration, but must be explicitly registered using a static @Bean
     * method when using @Configuration classes.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
