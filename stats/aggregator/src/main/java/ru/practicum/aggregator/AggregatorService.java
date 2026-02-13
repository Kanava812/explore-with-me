package ru.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.aggregator.config.KafkaProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(KafkaProperties.class)
public class AggregatorService {
    public static void main(String[] args) {
        SpringApplication.run(AggregatorService.class, args);
    }
}