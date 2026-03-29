package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.chat.SendMessageRequest;
import club.boyuan.official.teammatching.dto.request.chat.StompSendMessageRequest;
import club.boyuan.official.teammatching.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * 基于 STOMP 的聊天控制器：
 * 前端通过 WebSocket 连接 /ws-chat 后，
 * 向 /app/chat/send 发送消息，即可完成「发消息 + 推送」。
 */
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final MessageService messageService;

    /**
     * 通过 STOMP 发送聊天消息。
     * 目的地：/app/chat/send
     */
    @MessageMapping("/chat/send")
    public void sendChatMessage(@Valid StompSendMessageRequest payload) {
        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId(payload.getSessionId());
        request.setReceiverId(payload.getReceiverId());
        request.setContent(payload.getContent());
        request.setMsgType(payload.getMsgType());

        // 复用原有业务逻辑：落库 + 更新会话 + WebSocket 推送
        messageService.sendMessage(payload.getSenderId(), request);
    }
}

