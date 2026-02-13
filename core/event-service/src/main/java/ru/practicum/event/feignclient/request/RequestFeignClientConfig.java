package ru.practicum.event.feignclient.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RequestFeignClientConfig {
    @Bean
    public ErrorDecoder requestErrorDecoder(ObjectMapper objectMapper) {
        return new RequestFeignClientErrorDecoder(objectMapper);
    }
}

