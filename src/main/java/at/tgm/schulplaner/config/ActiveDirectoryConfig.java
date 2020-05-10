package at.tgm.schulplaner.config;

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
@ConfigurationProperties("active-directory")
@EnableConfigurationProperties
public class ActiveDirectoryConfig {
    private String url;
    private String domain;
    private String root;
    private String filter = "";
}
