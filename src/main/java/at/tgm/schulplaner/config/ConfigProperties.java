package at.tgm.schulplaner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * @author Georg Burkl
 * @version 2020-05-10
 */
@Data
@Configuration
@ConfigurationProperties("app")
@EnableConfigurationProperties
public class ConfigProperties {
    private Collection<String> ignoredPaths;
    private Collection<String> secureEndpoints;
    private Collection<String> adminAccounts;
}
