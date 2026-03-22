package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.team.JoinConfirmRequest;
import club.boyuan.official.teammatching.dto.response.team.JoinConfirmResponse;
import club.boyuan.official.teammatching.entity.ChatSession;
import club.boyuan.official.teammatching.entity.TeamApplication;
import club.boyuan.official.teammatching.entity.TeamMember;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ChatSessionMapper;
import club.boyuan.official.teammatching.mapper.TeamApplicationMapper;
import club.boyuan.official.teammatching.mapper.TeamMemberMapper;
import club.boyuan.official.teammatching.service.TeamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 团队服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamMemberMapper teamMemberMapper;
    private final TeamApplicationMapper teamApplicationMapper;
    private final ChatSessionMapper chatSessionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JoinConfirmResponse joinConfirm(Integer userId, JoinConfirmRequest request) {
        log.info("确认入队 userId={}, projectId={}, sessionId={}", userId, request.getProjectId(), request.getSessionId());

        ChatSession session = chatSessionMapper.selectById(request.getSessionId());
        if (session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        // 安全校验：只允许确认与会话绑定的项目的入队请求
        if (session.getProjectId() == null || !session.getProjectId().equals(request.getProjectId())) {
            throw new BusinessException("会话与项目不匹配");
        }

        // 确认入队人必须是该会话参与方之一
        boolean isParticipant = userId.equals(session.getUser1Id()) || userId.equals(session.getUser2Id());
        if (!isParticipant) {
            throw new BusinessException("无权限操作该会话");
        }

        // 防重复入队：已在当前项目的 team_member 中且状态为在队(0) 时禁止重复加入
        LambdaQueryWrapper<TeamMember> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(TeamMember::getProjectId, request.getProjectId())
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, 0);
        Long exist = teamMemberMapper.selectCount(existWrapper);
        if (exist != null && exist > 0) {
            throw new BusinessException("已在团队中");
        }

        // 从最近一条投递记录中推断在团队中的角色字段（如果没有就使用“成员”占位）
        String role = "成员";
        LambdaQueryWrapper<TeamApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(TeamApplication::getApplicantUserId, userId)
                .eq(TeamApplication::getProjectId, request.getProjectId())
                .orderByDesc(TeamApplication::getApplyTime);
        TeamApplication application = teamApplicationMapper.selectOne(appWrapper.last("LIMIT 1"));
        if (application != null && application.getRole() != null && !application.getRole().isBlank()) {
            role = application.getRole();
        }

        TeamMember member = new TeamMember();
        member.setProjectId(request.getProjectId());
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(java.time.LocalDateTime.now());
        member.setStatus(0);
        member.setUpdateTime(java.time.LocalDateTime.now());
        teamMemberMapper.insert(member);

        JoinConfirmResponse resp = new JoinConfirmResponse();
        resp.setMessage("恭喜你加入团队！");
        resp.setTeamId(request.getProjectId());
        resp.setSuggestHideTalent(true);
        return resp;
    }
}