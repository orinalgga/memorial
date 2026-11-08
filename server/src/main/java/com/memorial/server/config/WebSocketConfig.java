package com.memorial.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * 실시간 연결(STOMP). 글쓰기는 REST로 두고, 서버는 바뀐 내용을 여기로 알린다.
 * - /topic/spaces/{space}/events   : 새 사연·댓글·공감·촛불·채팅
 * - /topic/spaces/{space}/presence : 그 공간에 있는 사람의 위치와 모습
 * - /topic/memorials               : 새 개인 추모 공간
 * - /topic/visitors                : 공간마다 지금 머무는 사람 수
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;

    public WebSocketConfig(@Value("${memorial.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 하트비트: 휴대폰이 신호 없이 끊겨도 서버가 알아채고 그 사람을 공간에서 내보낸다.
        ThreadPoolTaskScheduler heartbeat = new ThreadPoolTaskScheduler();
        heartbeat.setPoolSize(1);
        heartbeat.setThreadNamePrefix("ws-heartbeat-");
        heartbeat.initialize();
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {25_000, 25_000})
                .setTaskScheduler(heartbeat);
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 위치 메시지는 작다. 큰 메시지로 서버 메모리를 쓰지 못하게 막는다.
        registration.setMessageSizeLimit(8 * 1024);
    }
}
