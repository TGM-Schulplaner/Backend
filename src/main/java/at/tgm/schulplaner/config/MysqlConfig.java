package at.tgm.schulplaner.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Georg Burkl
 * @version 2020-05-09
 */
@Slf4j
@Profile("!dev")
@Configuration
public class MysqlConfig {
}
