package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.chat.SendMessageRequest;
import club.boyuan.official.teammatching.dto.request.chat.UpdateRecruitStatusRequest;
import club.boyuan.official.teammatching.dto.response.chat.ChatMessageResponse;
import club.boyuan.official.teammatching.dto.response.chat.ChatSessionResponse;
import club.boyuan.official.teammatching.dto.response.chat.SendMessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 获取会话列表
     */
    List<ChatSessionResponse> getChatSessions(Integer userId, Integer page, Integer size);

    /**
     * 发送消息
     */
    SendMessageResponse sendMessage(Integer senderId, SendMessageRequest request);

    /**
     * 获取消息历史
     */
    List<ChatMessageResponse> getChatMessages(Integer userId, Integer sessionId, LocalDateTime before, Integer page, Integer size);

    /**
     * 更新沟通状态
     */
    Map<String, String> updateRecruitStatus(Integer userId, Integer sessionId, UpdateRecruitStatusRequest request);
}