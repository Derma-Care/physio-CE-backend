package com.clinicadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clinicadmin.exceptions.CustomFeignErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
//import feign.jackson.JacksonDecoder;
//import feign.jackson.JacksonDecoder;

@Configuration
public class FeignConfig {

    // ✅ Already existing — no change
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }

//    // ✅ Add this — fixes LocalDateTime deserialization in Feign
//    @Bean
//    public Decoder feignDecoder() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return new JacksonDecoder(mapper);
//    }
}