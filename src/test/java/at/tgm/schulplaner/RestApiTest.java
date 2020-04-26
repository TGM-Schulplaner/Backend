package at.tgm.schulplaner;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author Georg Burkl
 * @version 2020-04-21
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest
@ContextConfiguration
class RestApiTest {
    @Autowired
    private WebTestClient webClient;
}
