package at.tgm.schulplaner.security;

import at.tgm.schulplaner.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

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
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
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
                .cors()
                .configurationSource(corsConfigurationSource)
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/v1/login").permitAll()
                .pathMatchers(secureEndpoints.toArray(String[]::new)).authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

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

    @Bean
    WebFluxProperties webFluxProperties() {
        return new WebFluxProperties();
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
