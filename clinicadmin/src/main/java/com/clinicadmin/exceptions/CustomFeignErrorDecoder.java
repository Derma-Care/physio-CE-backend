package com.clinicadmin.exceptions;

import com.clinicadmin.dto.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class CustomFeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();
    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            ResponseStructure<?> error = mapper.readValue(bodyIs, ResponseStructure.class);
            return new FeignClientException(response.status(), error.getMessage());
        } catch (Exception e) {
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
