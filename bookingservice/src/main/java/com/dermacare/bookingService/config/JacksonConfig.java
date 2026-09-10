package com.dermacare.bookingService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Single shared ObjectMapper bean.
 * Jackson's ObjectMapper is thread-safe and is meant to be reused —
 * constructing a new one per request rebuilds its internal
 * serializer/deserializer caches every time, which was previously
 * happening 20+ times per request cycle across BookingService_ServiceImpl.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}