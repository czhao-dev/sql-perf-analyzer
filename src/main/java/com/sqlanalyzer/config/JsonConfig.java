package com.sqlanalyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot's Jackson autoconfiguration requires spring-web on the classpath
 * (for Jackson2ObjectMapperBuilder), which this CLI-only app intentionally omits.
 * Provide the ObjectMapper bean directly instead.
 */
@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
