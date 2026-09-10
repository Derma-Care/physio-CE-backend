package com.clinicadmin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Frontend connection endpoint:
        // ws://localhost:8080/clinic-admin/ws
        // or http://localhost:8080/clinic-admin/ws (SockJS)
        registry.addEndpoint("/clinic-admin/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Destinations that clients can subscribe to
        // Example:
        // /topic/notifications
        // /topic/appointments
        // /queue/private
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages sent from frontend to @MessageMapping methods
        // Example:
        // Client sends to: /clinic-admin/notify
        // @MessageMapping("/notify")
        registry.setApplicationDestinationPrefixes("/clinic-admin");
    }
}