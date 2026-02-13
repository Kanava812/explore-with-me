package ru.practicum.request.feignclient.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UserFeignClientConfig {
    @Bean
    public ErrorDecoder userErrorDecoder(ObjectMapper objectMapper) {
        return new UserFeignClientErrorDecoder(objectMapper);
    }
}




