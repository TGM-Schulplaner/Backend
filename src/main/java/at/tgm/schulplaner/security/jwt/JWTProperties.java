package at.tgm.schulplaner.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@Data
@Configuration
@ConfigurationProperties("jwt")
@EnableConfigurationProperties
class JWTProperties {
    private String secret;
    private long expiration;
}
