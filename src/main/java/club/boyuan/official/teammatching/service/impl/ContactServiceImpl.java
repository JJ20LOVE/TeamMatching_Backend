package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRequest;
import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRespondRequest;
import club.boyuan.official.teammatching.dto.response.contact.ContactExchangeResponse;
import club.boyuan.official.teammatching.dto.response.contact.ContactInfoResponse;
import club.boyuan.official.teammatching.entity.ChatSession;
import club.boyuan.official.teammatching.entity.ContactExchange;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ChatSessionMapper;
import club.boyuan.official.teammatching.mapper.ContactExchangeMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.ContactService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactExchangeMapper contactExchangeMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContactExchangeResponse requestExchange(Integer requesterId, ContactExchangeRequest request) {
        log.info("发起联系方式交换 requesterId={}, receiverId={}, projectId={}",
                requesterId, request.getReceiverId(), request.getProjectId());

        ChatSession session = chatSessionMapper.selectById(request.getSessionId());
        if (session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        // 只允许会话参与双方发起交换请求
        boolean isParticipant = Objects.equals(session.getUser1Id(), requesterId)
                || Objects.equals(session.getUser2Id(), requesterId);
        if (!isParticipant) {
            throw new BusinessException("无权限在该会话发起交换请求");
        }
        // 接收方也必须在同一个会话中
        if (!Objects.equals(request.getReceiverId(), session.getUser1Id())
                && !Objects.equals(request.getReceiverId(), session.getUser2Id())) {
            throw new BusinessException("接收方不属于该会话");
        }
        if (Objects.equals(requesterId, request.getReceiverId())) {
            throw new BusinessException("接收方不能是自己");
        }

        Integer projectId = request.getProjectId();
        if (projectId == null) {
            // 未显式指定项目时，默认绑定到会话关联的项目
            projectId = session.getProjectId();
        }

        LambdaQueryWrapper<ContactExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactExchange::getRequesterId, requesterId)
                .eq(ContactExchange::getReceiverId, request.getReceiverId())
                .eq(ContactExchange::getProjectId, projectId);
        ContactExchange exist = contactExchangeMapper.selectOne(wrapper);

        // 同一对 requester/receiver/project 只保留一条交换记录，避免重复插入
        ContactExchange exchange;
        if (exist != null) {
            exchange = exist;
        } else {
            exchange = new ContactExchange();
            exchange.setRequesterId(requesterId);
            exchange.setReceiverId(request.getReceiverId());
            exchange.setProjectId(projectId);
            exchange.setStatus(0);
            exchange.setRequestTime(LocalDateTime.now());
            contactExchangeMapper.insert(exchange);
        }

        ContactExchangeResponse resp = new ContactExchangeResponse();
        resp.setExchangeId(exchange.getExchangeId());
        resp.setMessage("请求已发送，等待对方确认");
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> respondExchange(Integer operatorId, Integer exchangeId, ContactExchangeRespondRequest request) {
        log.info("处理联系方式交换 operatorId={}, exchangeId={}, agree={}",
                operatorId, exchangeId, request.getAgree());

        ContactExchange exchange = contactExchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new ResourceNotFoundException("交换请求不存在");
        }
        // 只有接收方可以处理该交换请求
        if (!Objects.equals(exchange.getReceiverId(), operatorId)) {
            throw new BusinessException("无权限处理该交换请求");
        }
        // 已处理过的请求不能重复处理
        if (exchange.getStatus() != null && exchange.getStatus() != 0) {
            throw new BusinessException("该交换请求已处理");
        }

        boolean agree = Boolean.TRUE.equals(request.getAgree());
        exchange.setStatus(agree ? 1 : 2);
        exchange.setResponseTime(LocalDateTime.now());
        contactExchangeMapper.updateById(exchange);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "操作成功");

        if (agree) {
            // 同意后返回发起方的脱敏联系方式，由前端决定展示给谁
            User requester = userMapper.selectById(exchange.getRequesterId());
            if (requester == null) {
                throw new ResourceNotFoundException("用户不存在");
            }
            ContactInfoResponse contactInfo = toContactInfo(requester);
            resp.put("contactInfo", contactInfo);
        }

        return resp;
    }

    @Override
    public ContactInfoResponse getContactInfo(Integer operatorId, Integer targetUserId) {
        log.info("获取联系方式 operatorId={}, targetUserId={}", operatorId, targetUserId);

        // 需要双方存在“已同意”的交换记录（任意方向，任意项目），用作访问控制
        LambdaQueryWrapper<ContactExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContactExchange::getStatus, 1)
                .and(w -> w
                        .and(w1 -> w1.eq(ContactExchange::getRequesterId, operatorId)
                                .eq(ContactExchange::getReceiverId, targetUserId))
                        .or()
                        .and(w2 -> w2.eq(ContactExchange::getRequesterId, targetUserId)
                                .eq(ContactExchange::getReceiverId, operatorId)));

        Long count = contactExchangeMapper.selectCount(wrapper);
        if (count == null || count <= 0) {
            throw new BusinessException("未完成联系方式交换，无法查看对方联系方式");
        }

        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        return toContactInfo(target);
    }

    private static ContactInfoResponse toContactInfo(User user) {
        ContactInfoResponse resp = new ContactInfoResponse();
        resp.setPhone(maskPhone(user.getPhone()));
        resp.setEmail(maskEmail(user.getEmail()));
        // 当前 user 表没有 wechat/qq 字段，这里先返回空；如后续补字段可在此映射
        resp.setWechat("");
        resp.setQq("");
        return resp;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone == null ? "" : phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email == null ? "" : email;
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
    }
}

