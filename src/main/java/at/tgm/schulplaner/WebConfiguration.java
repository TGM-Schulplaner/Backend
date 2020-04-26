package at.tgm.schulplaner;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author Georg Burkl
 * @version 2020-04-26
 */
@Configuration
public class WebConfiguration implements WebFluxConfigurer {
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(Pageable.class);
            }

            @Override
            public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
                MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
                try {
                    String pageString = queryParams.getFirst("page");
                    String sizeString = queryParams.getFirst("size");
                    Objects.requireNonNull(pageString);
                    Objects.requireNonNull(sizeString);
                    int page = Integer.parseInt(pageString);
                    int size = Integer.parseInt(sizeString);
                    return Mono.just(PageRequest.of(page, size));
                } catch (NullPointerException | NumberFormatException exception) {
                    return Mono.just(PageRequest.of(0, 10));
                }
            }
        });
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }
}
