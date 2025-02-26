package com.travelbuddy.config;

import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.security.CustomUserDetailsService;
import com.travelbuddy.security.JwtTokenProvider;
import com.travelbuddy.service.ChatRoomService;
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
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ChatRoomService chatRoomService;

    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("/room/(\\d+)/.*");

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
                    // Extract JWT token and authenticate
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.get(0).substring(7);
                        if (jwtTokenProvider.validateToken(token)) {
                            String username = jwtTokenProvider.extractUsername(token);
                            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);
                        }
                    }

                    // Handle room subscriptions
                    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        if (destination != null) {
                            Matcher matcher = ROOM_ID_PATTERN.matcher(destination);
                            if (matcher.matches()) {
                                Long roomId = Long.parseLong(matcher.group(1));
                                String username = accessor.getUser().getName();
                                chatRoomService.addUserToRoom(roomId, username);
                            }
                        }
                    }
                }

                return message;
            }
        });
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = accessor.getUser().getName();

        // Find all rooms the user was in and remove them
        // This could be optimized by maintaining a user-to-rooms mapping
        Pattern pattern = Pattern.compile("/room/(\\d+)/.*");
        Objects.requireNonNull(accessor.getSessionAttributes()).forEach((key, value) -> {
            if (key != null && ((String) key).startsWith("/room/")) {
                Matcher matcher = pattern.matcher((String) key);
                if (matcher.matches()) {
                    Long roomId = Long.parseLong(matcher.group(1));
                    chatRoomService.removeUserFromRoom(roomId, username);
                }
            }
        });
    }
}