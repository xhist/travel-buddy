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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

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
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null) {
                    // For all messages, including SEND
                    String token = null;
                    List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
                    if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                        token = authorizationHeaders.getFirst();
                    }

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            if (jwtTokenProvider.validateToken(token)) {
                                String username = jwtTokenProvider.extractUsername(token);
                                CustomUserDetails userDetails =
                                        (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                SecurityContextHolder.getContext().setAuthentication(auth);
                                accessor.setUser(auth);

                                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                                    onlineUserService.userConnected(username);
                                    log.info("User authenticated via WebSocket: {}", username);
                                }
                            }
                        } catch (Exception e) {
                            log.error("WebSocket Authentication error", e);
                        }
                    }

                    if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        Principal user = accessor.getUser();
                        if (user != null) {
                            onlineUserService.userDisconnected(user.getName());
                            SecurityContextHolder.clearContext();
                        }
                    }
                }
                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                SecurityContextHolder.clearContext();
            }
        });
    }
}