package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.analyzer.config.DataSourceConfig;
import ru.practicum.analyzer.config.KafkaProperties;


@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties({KafkaProperties.class, DataSourceConfig.class})
public class AnalyzerService {
    public static void main(String[] args) {
        logArguments(args); // здесь добавляем логирование аргументов
        SpringApplication.run(AnalyzerService.class, args); // передаем args в run()
    }

    private static void logArguments(String[] args) {
        System.out.println("Command-line arguments received:");
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("Arg [" + i + "] = " + args[i]);
            }
        } else {
            System.out.println("No command-line arguments provided.");
        }
    }
}