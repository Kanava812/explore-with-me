package ru.practicum.request.feignclient.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import ru.practicum.internal.error.ApiError;
import ru.practicum.internal.error.exception.BadRequestException;
import ru.practicum.internal.error.exception.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class UserFeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    private final ObjectMapper objectMapper;

    public UserFeignClientErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("UserClient methodKey: {}, response: {}", methodKey, response);

        try {
            if (response.status() == 400) {
                ApiError error = parseErrorBody(response);
                return new BadRequestException(error.getMessage());
            }
            if (response.status() == 404) {
                ApiError error = parseErrorBody(response);
                return new NotFoundException(error.getMessage());
            }
        } catch (Exception e) {
            log.warn("Ошибка в UserClientErrorDecoder для метода {}:", methodKey, e);
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
            log.warn("Ошибка десериализации тела ответа {}: ", response, e);
        }
        return null;
    }
}
