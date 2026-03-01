package club.boyuan.official.teammatching.websocket;

import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket服务端
 */
@Component
@ServerEndpoint("/websocket")
public class WebSocketServer {
    
    @OnOpen
    public void onOpen(Session session) {
        // 连接打开处理
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // 消息处理
    }
    
    @OnClose
    public void onClose(Session session) {
        // 连接关闭处理
    }
}