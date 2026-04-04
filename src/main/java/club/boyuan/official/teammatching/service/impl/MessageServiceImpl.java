package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.chat.SendMessageRequest;
import club.boyuan.official.teammatching.dto.request.chat.UpdateRecruitStatusRequest;
import club.boyuan.official.teammatching.dto.response.chat.ChatMessageResponse;
import club.boyuan.official.teammatching.dto.response.chat.ChatSessionResponse;
import club.boyuan.official.teammatching.dto.response.chat.SendMessageResponse;
import club.boyuan.official.teammatching.entity.ChatSession;
import club.boyuan.official.teammatching.entity.Message;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ChatSessionMapper;
import club.boyuan.official.teammatching.mapper.MessageMapper;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.mq.producer.NotificationProducer;
import club.boyuan.official.teammatching.mq.support.NotificationPreferenceUtils;
import club.boyuan.official.teammatching.service.MessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final String USER_CACHE_NAME = "user";

    private final ChatSessionMapper chatSessionMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final CacheManager userCacheManager;
    private final NotificationProducer notificationProducer;

    public MessageServiceImpl(ChatSessionMapper chatSessionMapper, MessageMapper messageMapper,
                              UserMapper userMapper, ProjectMapper projectMapper,
                              SimpMessagingTemplate messagingTemplate,
                              @Qualifier("userCacheManager") CacheManager userCacheManager,
                              NotificationProducer notificationProducer) {
        this.chatSessionMapper = chatSessionMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
        this.messagingTemplate = messagingTemplate;
        this.userCacheManager = userCacheManager;
        this.notificationProducer = notificationProducer;
    }

    @Override
    public List<ChatSessionResponse> getChatSessions(Integer userId, Integer page, Integer size) {
        // 防御性处理分页参数：提供默认值并限制单页最大数量，避免一次性拉取过多会话
        long current = page == null ? 1L : Math.max(1L, page.longValue());
        long pageSize = size == null ? 20L : Math.max(1L, Math.min(100L, size.longValue()));

        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        // 仅查询正常状态的会话，并且当前用户作为 user1 或 user2 参与其中
        wrapper.eq(ChatSession::getStatus, 1)
                .and(w -> w.eq(ChatSession::getUser1Id, userId).or().eq(ChatSession::getUser2Id, userId))
                // 按最近一条消息时间 & 更新时间倒序，用于消息中心主列表排序
                .orderByDesc(ChatSession::getLastMsgTime)
                .orderByDesc(ChatSession::getUpdateTime);

        Page<ChatSession> p = new Page<>(current, pageSize);
        List<ChatSession> sessions = chatSessionMapper.selectPage(p, wrapper).getRecords();
        if (sessions == null || sessions.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算“对方用户ID”：如果当前是 user1，则对方是 user2，反之亦然
        List<Integer> otherIds = sessions.stream()
                .map(s -> Objects.equals(s.getUser1Id(), userId) ? s.getUser2Id() : s.getUser1Id())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, User> userMap = otherIds.isEmpty()
                ? new HashMap<>()
                : userMapper.selectBatchIds(otherIds).stream().collect(Collectors.toMap(User::getUserId, u -> u));

        // 收集所有关联的项目ID，用于批量查询项目名称，避免 N+1 查询
        List<Integer> projectIds = sessions.stream()
                .map(ChatSession::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, Project> projectMap = projectIds.isEmpty()
                ? new HashMap<>()
                : projectMapper.selectBatchIds(projectIds).stream().collect(Collectors.toMap(Project::getProjectId, pr -> pr));

        List<ChatSessionResponse> result = new ArrayList<>();
        for (ChatSession s : sessions) {
            Integer otherId = Objects.equals(s.getUser1Id(), userId) ? s.getUser2Id() : s.getUser1Id();
            User other = otherId == null ? null : userMap.get(otherId);

            ChatSessionResponse resp = new ChatSessionResponse();
            resp.setSessionId(s.getSessionId());
            resp.setLastMessage(s.getLastMessage());
            resp.setLastMsgTime(s.getLastMsgTime());
            // 根据当前用户是 user1 还是 user2，返回对应侧的未读数
            resp.setUnreadCount(Objects.equals(s.getUser1Id(), userId)
                    ? nullToZero(s.getUser1Unread())
                    : nullToZero(s.getUser2Unread()));
            resp.setRecruitStatus(s.getRecruitStatus() == null ? "communicating" : s.getRecruitStatus());
            resp.setProjectId(s.getProjectId());

            Project pr = s.getProjectId() == null ? null : projectMap.get(s.getProjectId());
            resp.setProjectName(pr == null ? null : pr.getName());

            ChatSessionResponse.TargetUser target = new ChatSessionResponse.TargetUser();
            if (other != null) {
                target.setUserId(other.getUserId());
                target.setNickname(other.getNickname());
                target.setAvatar(other.getAvatarFileId() == null ? null : other.getAvatarFileId().toString());
            } else {
                target.setUserId(otherId);
                target.setNickname("");
                target.setAvatar("");
            }
            resp.setTargetUser(target);
            result.add(resp);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse sendMessage(Integer senderId, SendMessageRequest request) {
        ChatSession session = chatSessionMapper.selectById(request.getSessionId());
        if (session == null || session.getStatus() == null || session.getStatus() != 1) {
            throw new ResourceNotFoundException("会话不存在");
        }

        // 权限校验：只有会话参与方才能在该会话内发送消息
        boolean isParticipant = Objects.equals(session.getUser1Id(), senderId) || Objects.equals(session.getUser2Id(), senderId);
        if (!isParticipant) {
            throw new BusinessException("无权限在该会话发送消息");
        }

        Integer receiverId = request.getReceiverId();
        // 接收方必须是该会话的另一个参与者
        if (!Objects.equals(receiverId, session.getUser1Id()) && !Objects.equals(receiverId, session.getUser2Id())) {
            throw new BusinessException("接收方不属于该会话");
        }
        // 防止自己给自己发消息的错误使用场景
        if (Objects.equals(receiverId, senderId)) {
            throw new BusinessException("接收方不能是自己");
        }

        LocalDateTime now = LocalDateTime.now();

        Message message = new Message();
        message.setSessionId(request.getSessionId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType());
        message.setStatus(0);
        message.setSendTime(now);
        messageMapper.insert(message);

        // 将消息内容写入会话表，便于会话列表直接展示最后一条消息摘要
        session.setLastMessage(request.getContent());
        session.setLastMsgTime(now);
        session.setUpdateTime(now);
        // 为接收方累加未读数，用于红点提醒
        if (Objects.equals(session.getUser1Id(), receiverId)) {
            session.setUser1Unread(nullToZero(session.getUser1Unread()) + 1);
        } else {
            session.setUser2Unread(nullToZero(session.getUser2Unread()) + 1);
        }
        chatSessionMapper.updateById(session);

        // 构造单条消息的响应 DTO，用于 WebSocket 推送给会话双方（发送方信息走短时缓存，避免每次发消息都查库）
        User sender = getSenderForPush(senderId);
        ChatMessageResponse push = new ChatMessageResponse();
        push.setMessageId(message.getMessageId());
        push.setSenderId(senderId);
        push.setSenderNickname(sender == null ? "" : sender.getNickname());
        push.setSenderAvatar(sender == null || sender.getAvatarFileId() == null
                ? null
                : sender.getAvatarFileId().toString());
        push.setContent(message.getContent());
        push.setMsgType(message.getMsgType());
        push.setStatus(message.getStatus());
        push.setSendTime(message.getSendTime());

        // 通过 STOMP 向 /topic/chat/{sessionId} 广播新消息
        // 前端只需订阅该 topic 即可实时收到消息，而无需轮询 HTTP 接口
        messagingTemplate.convertAndSend("/topic/chat/" + session.getSessionId(), push);

        User receiver = userMapper.selectById(receiverId);
        if (receiver != null && NotificationPreferenceUtils.isChannelEnabled(receiver.getMessageNotify())) {
            User senderUser = getSenderForPush(senderId);
            String nickname = senderUser == null || senderUser.getNickname() == null ? "对方" : senderUser.getNickname();
            String preview = abbreviate(request.getContent(), 120);
            notificationProducer.publishMessage(
                    receiverId,
                    "新消息",
                    nickname + "：" + preview,
                    "chat_message",
                    String.valueOf(session.getSessionId()));
        }

        SendMessageResponse resp = new SendMessageResponse();
        resp.setMessageId(message.getMessageId());
        resp.setSendTime(now);
        return resp;
    }

    /**
     * 获取用于推送的发送方用户信息：优先从短时缓存读取，未命中再查库并写入缓存。
     * 避免每次发消息都查 user 表。
     */
    private User getSenderForPush(Integer userId) {
        Cache cache = userCacheManager.getCache(USER_CACHE_NAME);
        if (cache == null) {
            return userMapper.selectById(userId);
        }
        User cached = cache.get(userId, User.class);
        if (cached != null) {
            return cached;
        }
        User user = userMapper.selectById(userId);
        if (user != null) {
            cache.put(userId, user);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChatMessageResponse> getChatMessages(Integer userId, Integer sessionId, LocalDateTime before, Integer page, Integer size) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        // 只有会话参与方能查看该会话的消息历史
        boolean isParticipant = Objects.equals(session.getUser1Id(), userId) || Objects.equals(session.getUser2Id(), userId);
        if (!isParticipant) {
            throw new BusinessException("无权限查看该会话消息");
        }

        // 与会话列表类似的分页保护
        long current = page == null ? 1L : Math.max(1L, page.longValue());
        long pageSize = size == null ? 20L : Math.max(1L, Math.min(100L, size.longValue()));

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSessionId, sessionId);
        if (before != null) {
            // 用 before 做“向上滚动分页”，只查指定时间之前的消息
            wrapper.lt(Message::getSendTime, before);
        }
        wrapper.orderByDesc(Message::getSendTime);

        Page<Message> p = new Page<>(current, pageSize);
        List<Message> messages = messageMapper.selectPage(p, wrapper).getRecords();
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        // 将当前用户作为接收方的未读消息全部标记为已读
        LambdaQueryWrapper<Message> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Message::getSessionId, sessionId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getStatus, 0);
        Message update = new Message();
        update.setStatus(1);
        messageMapper.update(update, unreadWrapper);

        // 同步清零 chat_session 中对应侧的未读计数
        if (Objects.equals(session.getUser1Id(), userId)) {
            session.setUser1Unread(0);
        } else {
            session.setUser2Unread(0);
        }
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);

        List<Integer> senderIds = messages.stream().map(Message::getSenderId).filter(Objects::nonNull).distinct().toList();
        Map<Integer, User> userMap = senderIds.isEmpty()
                ? new HashMap<>()
                : userMapper.selectBatchIds(senderIds).stream().collect(Collectors.toMap(User::getUserId, u -> u));

        // 数据库按时间倒序拉取，这里转为按时间正序返回，适合前端按时间线展示
        List<Message> asc = new ArrayList<>(messages);
        asc.sort(Comparator.comparing(Message::getSendTime));

        List<ChatMessageResponse> result = new ArrayList<>();
        for (Message m : asc) {
            User u = userMap.get(m.getSenderId());
            ChatMessageResponse resp = new ChatMessageResponse();
            resp.setMessageId(m.getMessageId());
            resp.setSenderId(m.getSenderId());
            resp.setSenderNickname(u == null ? "" : u.getNickname());
            resp.setSenderAvatar(u == null || u.getAvatarFileId() == null ? null : u.getAvatarFileId().toString());
            resp.setContent(m.getContent());
            resp.setMsgType(m.getMsgType());
            resp.setStatus(m.getStatus());
            resp.setSendTime(m.getSendTime());
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateRecruitStatus(Integer userId, Integer sessionId, UpdateRecruitStatusRequest request) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }

        // 只有会话参与方可以更新本会话的沟通状态
        boolean isParticipant = Objects.equals(session.getUser1Id(), userId) || Objects.equals(session.getUser2Id(), userId);
        if (!isParticipant) {
            throw new BusinessException("无权限更新该会话状态");
        }

        String status = request.getRecruitStatus();
        // 为避免写入非法字符串，只允许接口文档里枚举的三种值
        if (!List.of("communicating", "offer", "reject").contains(status)) {
            throw new BusinessException("recruitStatus参数错误");
        }

        session.setRecruitStatus(status);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);

        Map<String, String> result = new HashMap<>();
        result.put("recruitStatus", status);
        return result;
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static String abbreviate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }
}