package com.travelbuddy.config;

import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.security.CustomUserDetailsService;
import com.travelbuddy.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MappingJackson2MessageConverter mappingJackson2MessageConverter;

    // Store last typing status time to prevent excessive typing updates
    private final Map<String, Map<String, Long>> typingStatusTimes = new ConcurrentHashMap<>();

    // Debounce period for typing status in milliseconds
    private static final long TYPING_DEBOUNCE_MS = 2000;

    // Pattern matching for different room-related destinations
    private static final Pattern ROOM_SUBSCRIBE_PATTERN = Pattern.compile("/topic/room/(\\d+)(?:/.*)?");
    private static final Pattern TYPING_PATTERN = Pattern.compile("/app/room\\.(\\d+)\\.typing");
    private static final Pattern PRIVATE_TYPING_PATTERN = Pattern.compile("/app/chat\\.typing");

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
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(mappingJackson2MessageConverter);
        return false;
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

                            // Store username in session attributes for later retrieval
                            accessor.getSessionAttributes().put("username", username);
                        }
                    }

                    // Handle typing status updates with debounce logic
                    if (StompCommand.SEND.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        if (destination != null) {
                            // Handle room typing events
                            Matcher roomTypingMatcher = TYPING_PATTERN.matcher(destination);
                            if (roomTypingMatcher.matches()) {
                                String username = accessor.getUser().getName();
                                String roomId = roomTypingMatcher.group(1);
                                boolean shouldProcess = shouldProcessTypingUpdate(username, roomId);

                                if (!shouldProcess) {
                                    // Skip this message if we recently processed one
                                    return null;
                                }
                            }

                            // Handle private chat typing events
                            Matcher privateTypingMatcher = PRIVATE_TYPING_PATTERN.matcher(destination);
                            if (privateTypingMatcher.matches()) {
                                String username = accessor.getUser().getName();

                                // Get recipient from message payload if possible
                                Object payload = accessor.getMessageHeaders().get("payload");
                                if (payload instanceof Map) {
                                    String recipient = (String) ((Map)payload).get("recipient");
                                    if (recipient != null) {
                                        boolean shouldProcess = shouldProcessTypingUpdate(username, recipient);

                                        if (!shouldProcess) {
                                            // Skip this message if we recently processed one
                                            return null;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Handle room subscriptions with improved pattern matching
                    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        if (destination != null) {
                            Matcher matcher = ROOM_SUBSCRIBE_PATTERN.matcher(destination);
                            if (matcher.matches()) {
                                Long roomId = Long.parseLong(matcher.group(1));
                                String username = accessor.getUser().getName();
                                log.debug("User {} subscribing to room {}", username, roomId);

                                // Store subscription info in session for cleanup on disconnect
                                accessor.getSessionAttributes().put("room_" + roomId, Boolean.TRUE);

                                // Instead of direct call, publish a room join event
                                eventPublisher.publishEvent(new RoomJoinEvent(username, roomId));
                            }
                        }
                    }
                }

                return message;
            }
        });
    }

    /**
     * Check if we should process a typing update based on debounce logic
     */
    private boolean shouldProcessTypingUpdate(String username, String targetId) {
        long now = System.currentTimeMillis();

        // Ensure we have maps for this user
        typingStatusTimes.putIfAbsent(username, new ConcurrentHashMap<>());
        Map<String, Long> userTypingTimes = typingStatusTimes.get(username);

        // Get last time we processed a typing update for this user+target
        Long lastTime = userTypingTimes.get(targetId);

        if (lastTime == null || (now - lastTime) > TYPING_DEBOUNCE_MS) {
            // It's been long enough, process this update
            userTypingTimes.put(targetId, now);
            return true;
        }

        // Too soon, skip this update
        return false;
    }
}