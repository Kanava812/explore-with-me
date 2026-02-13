package ru.practicum.event.feignclient.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import ru.practicum.internal.error.ApiError;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.error.exception.RuleViolationException;
import ru.practicum.internal.error.exception.ServiceUnavailableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RequestFeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    public RequestFeignClientErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("RequestClient methodKey: {}, response: {}", methodKey, response);

        try {
            if (response.status() == 404) {
                ApiError error = parseErrorBody(response);
                return new NotFoundException(error.getMessage());
            }

            if (response.status() == 409) {
                ApiError error = parseErrorBody(response);
                return new RuleViolationException(error.getMessage());
            }
            if (response.status() == 503) {
                ApiError error = parseErrorBody(response);
                return new ServiceUnavailableException(error.getMessage());
            }
        } catch (Exception e) {
            log.warn("Ошибка в RequestClientErrorDecoder для метода {}:", methodKey, e);
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private ApiError parseErrorBody(Response response) {
        try {
            if (response.body() != null) {
                String body = StreamUtils.copyToString(
                        response.body().asInputStream(),
                        StandardCharsets.UTF_8
                );

                if (!body.trim().isEmpty()) {
                    return objectMapper.readValue(body, ApiError.class);
                }
            }
        } catch (IOException e) {
            log.warn("Ошибка в RequestClientErrorDecoder для метода {}:", response, e);
        }
        return null;
    }
}
