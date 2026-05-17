package fr.redfroggy.bdd.exceltestrunner.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class that provides a RestTemplate bean for HTTP communication.
 * Spring Boot auto-configures RestTemplateBuilder but not RestTemplate itself.
 *
 * @author Ashish,Raut
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean using the auto-configured RestTemplateBuilder.
     *
     * @param builder the RestTemplateBuilder provided by Spring Boot
     * @return a configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
