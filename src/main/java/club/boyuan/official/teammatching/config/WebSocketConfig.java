package club.boyuan.official.teammatching.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket / STOMP 配置：
 * <p>后端通过 /topic 前缀广播消息，前端只需订阅对应的 topic 即可实时收到聊天消息。</p>
 * <p>通知：订阅 {@code /topic/notify/{当前用户ID}} 接收异步通知与通知设置同步（请勿订阅他人 ID）。</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 暴露 STOMP 端点，前端通过 ws://host/ws-chat 或 SockJS 连接
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单内存消息代理，前端可订阅 /topic/** 接收广播
        registry.enableSimpleBroker("/topic");
        // 约定应用级发送前缀为 /app（当前实现只用来广播，不处理 @MessageMapping）
        registry.setApplicationDestinationPrefixes("/app");
    }
}
