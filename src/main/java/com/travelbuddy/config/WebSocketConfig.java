package com.travelbuddy.config;

import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.security.CustomUserDetailsService;
import com.travelbuddy.security.JwtTokenProvider;
import com.travelbuddy.service.OnlineUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages bound for the message broker (server -> client)
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix for messages bound for @MessageMapping methods (client -> server)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Retrieve the token from the native headers (adjust header name if needed)
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        if (jwtTokenProvider.validateToken(token)) {
                            String username = jwtTokenProvider.extractUsername(token);
                            CustomUserDetails userDetails =
                                    (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
                            // Create an authentication object and set it in the accessor
                            accessor.setUser(
                                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    )
                            );

                            onlineUserService.userConnected(username);

                            log.info("User authenticated via WebSocket: {}", username);
                        }
                    }
                }
                else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    Principal user = accessor.getUser();
                    if (user != null) {
                        onlineUserService.userDisconnected(user.getName());
                    }
                }
                return message;
            }
        });
    }
}
