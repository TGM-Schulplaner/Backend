package at.tgm.schulplaner;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Collections;

/**
 * @author Georg Burkl
 * @version 2020-03-05
 */
@Slf4j
@Configuration
@PropertySource("classpath:application.yml")
@NoArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String ALLE = "alle";
    private static final String EVERYONE_PASSWORD = "everyonePassword";
    private static final String LEHRER = "lehrer";
    private static final String SCHUELER = "schueler";

    @Value("${active_directory.url}")    private String adUrl;
    @Value("${active_directory.domain}") private String adDomain;
    @Value("${active_directory.root}")   private String adRoot;
    @Value("${active_directory.filter}") private String adSearchFilter;
    @Value("${secure_endpoints}")        private String secureEndpoints;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers(secureEndpoints.split("\\|")).authenticated()
                .anyExchange().permitAll()
                .and().formLogin()
                .and().build();
    }

    @Bean
    ReactiveAuthenticationManager authenticationManager(UserDetailsContextMapper mapper) {

        ActiveDirectoryLdapAuthenticationProvider adldap = new ActiveDirectoryLdapAuthenticationProvider(adDomain, adUrl, adRoot);
        if (!adSearchFilter.isBlank())
            adldap.setSearchFilter(adSearchFilter);
        adldap.setUserDetailsContextMapper(mapper);
        /*adldap.setAuthoritiesMapper(authorities -> authorities.stream()
                .filter(a -> !a.getAuthority().equals(EVERYONE_PASSWORD))
                .filter(a -> !a.getAuthority().equals(ALLE))
                .flatMap(grantedAuthority -> {
                    String a = grantedAuthority.getAuthority();
                    if (a.startsWith(SCHUELER))
                        return Stream.of(
                                new SimpleGrantedAuthority(SCHUELER),
                                new SimpleGrantedAuthority(a.substring(SCHUELER.length())));
                    else if (a.startsWith(LEHRER))
                        return Stream.of(
                                new SimpleGrantedAuthority(LEHRER),
                                new SimpleGrantedAuthority(a.substring(LEHRER.length())));
                    else
                        return Stream.of(grantedAuthority);
                }).collect(Collectors.toSet()));*/

        AuthenticationManager am = new ProviderManager(Collections.singletonList(adldap));

        return new ReactiveAuthenticationManagerAdapter(am);
    }

    @Bean
    UserDetailsContextMapper mapper() {
        return new CustomLdapUserDetailsMapper();
    }

    /**
     * In order to resolve ${...} placeholders in definitions or @Value annotations using properties
     * from a PropertySource, one must register a PropertySourcesPlaceholderConfigurer. This happens
     * automatically when using XML configuration, but must be explicitly registered using a static
     * @Bean method when using @Configuration classes.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
