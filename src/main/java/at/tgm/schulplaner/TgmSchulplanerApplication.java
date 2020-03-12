package at.tgm.schulplaner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class TgmSchulplanerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TgmSchulplanerApplication.class, args);
    }
}
