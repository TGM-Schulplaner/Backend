package at.tgm.schulplaner.security;

import at.tgm.schulplaner.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
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
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) ->
                        Mono.fromRunnable(() ->
                                swe.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED)))
                .accessDeniedHandler((swe, e) ->
                        Mono.fromRunnable(() ->
                                swe.getResponse()
                                        .setStatusCode(HttpStatus.FORBIDDEN)))
                .and()
                .cors().and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().and()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers(secureEndpoints.toArray(String[]::new)).authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    ReactiveAuthenticationManager authenticationManager(UserDetailsContextMapper mapper) {
        ActiveDirectoryLdapAuthenticationProvider adLdap = new ActiveDirectoryLdapAuthenticationProvider(adDomain, adUrl, adRoot);
        if (!adSearchFilter.isBlank())
            adLdap.setSearchFilter(adSearchFilter);
        adLdap.setUserDetailsContextMapper(mapper);
        return new ReactiveAuthenticationManagerAdapter(new ProviderManager(adLdap));
    }

    @Bean
    UserDetailsContextMapper mapper(UserRepository repository) {
        return new CustomLdapUserDetailsMapper(repository);
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
