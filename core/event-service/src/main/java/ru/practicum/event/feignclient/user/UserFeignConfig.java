package ru.practicum.event.feignclient.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserFeignConfig {
    @Bean
    public ErrorDecoder userErrorDecoder(ObjectMapper objectMapper) {
        return new UserFeignClientErrorDecoder(objectMapper);
    }
}




