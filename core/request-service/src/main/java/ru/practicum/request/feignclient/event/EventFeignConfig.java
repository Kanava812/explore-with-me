package ru.practicum.request.feignclient.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventFeignConfig {
    @Bean
    public ErrorDecoder eventErrorDecoder(ObjectMapper objectMapper) {
        return new EventFeignClientErrorDecoder(objectMapper);
    }
}


