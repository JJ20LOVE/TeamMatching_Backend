package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.enums.TeamMemberStatusEnum;
import club.boyuan.official.teammatching.dto.request.team.CreateTaskRequest;
import club.boyuan.official.teammatching.dto.request.team.CreateTeamPostRequest;
import club.boyuan.official.teammatching.dto.request.team.UpdateTaskStatusRequest;
import club.boyuan.official.teammatching.dto.response.team.MyTeamListResponse;
import club.boyuan.official.teammatching.dto.response.team.TaskResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamDetailResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamMemberResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamPostListResponse;
import club.boyuan.official.teammatching.entity.ChatSession;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.TeamMember;
import club.boyuan.official.teammatching.entity.TeamPost;
import club.boyuan.official.teammatching.entity.TeamPostAttachment;
import club.boyuan.official.teammatching.entity.TeamTask;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ChatSessionMapper;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.TeamMemberMapper;
import club.boyuan.official.teammatching.mapper.TeamPostAttachmentMapper;
import club.boyuan.official.teammatching.mapper.TeamPostMapper;
import club.boyuan.official.teammatching.mapper.TeamTaskMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.TeamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 团队服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private static final int TEAM_POST_STATUS_NORMAL = 1;
    private static final int TEAM_TASK_TODO = 0;
    private static final int TEAM_TASK_IN_PROGRESS = 1;
    private static final int TEAM_TASK_COMPLETED = 2;
    private static final int CHAT_SESSION_STATUS_NORMAL = 1;
    private static final int FILE_TARGET_TYPE_TEAM_POST_ATTACHMENT = 9;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProjectMapper projectMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;
    private final FileResourceMapper fileResourceMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final TeamPostMapper teamPostMapper;
    private final TeamPostAttachmentMapper teamPostAttachmentMapper;
    private final TeamTaskMapper teamTaskMapper;

    @Override
    public MyTeamListResponse getMyTeams(Integer userId) {
        List<MyTeamListResponse.TeamBriefItem> leading = new ArrayList<>();
        List<MyTeamListResponse.TeamBriefItem> joining = new ArrayList<>();

        LambdaQueryWrapper<Project> leadingWrapper = new LambdaQueryWrapper<>();
        leadingWrapper.eq(Project::getPublisherUserId, userId)
                .orderByDesc(Project::getReleaseTime);
        List<Project> leadingProjects = projectMapper.selectList(leadingWrapper);
        for (Project project : leadingProjects) {
            MyTeamListResponse.TeamBriefItem item = buildTeamBrief(project, "队长", true, userId);
            leading.add(item);
        }

        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode())
                .orderByDesc(TeamMember::getJoinTime);
        List<TeamMember> memberships = teamMemberMapper.selectList(memberWrapper);
        if (!memberships.isEmpty()) {
            List<Integer> joinedProjectIds = memberships.stream()
                    .map(TeamMember::getProjectId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!joinedProjectIds.isEmpty()) {
                List<Project> joinedProjects = projectMapper.selectBatchIds(joinedProjectIds);
                for (Project project : joinedProjects) {
                    if (project == null || Objects.equals(project.getPublisherUserId(), userId)) {
                        continue;
                    }
                    MyTeamListResponse.TeamBriefItem item = buildTeamBrief(project, "队员", false, userId);
                    joining.add(item);
                }
            }
        }

        MyTeamListResponse response = new MyTeamListResponse();
        response.setLeading(leading);
        response.setJoining(joining);
        return response;
    }

    @Override
    public TeamDetailResponse getTeamDetail(Integer projectId, Integer userId) {
        Project project = validateProjectExists(projectId);
        ensureTeamMember(project, userId);

        TeamDetailResponse response = new TeamDetailResponse();
        response.setProjectId(project.getProjectId());
        response.setName(project.getName());
        response.setStatus(project.getStatus());
        response.setMembers(buildTeamMembers(project));
        response.setProgress(resolveProgress(project));
        response.setTaskStats(buildTaskStats(project.getProjectId()));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createTeamPost(Integer projectId, Integer userId, CreateTeamPostRequest request) {
        Project project = validateProjectExists(projectId);
        ensureTeamMember(project, userId);

        LocalDateTime now = LocalDateTime.now();
        TeamPost post = new TeamPost();
        post.setProjectId(projectId);
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(TEAM_POST_STATUS_NORMAL);
        post.setCreatedTime(now);
        post.setUpdateTime(now);
        teamPostMapper.insert(post);

        List<Long> attachmentIds = request.getAttachments();
        if (attachmentIds != null && !attachmentIds.isEmpty()) {
            List<Long> deduplicatedIds = new ArrayList<>(new LinkedHashSet<>(attachmentIds));
            for (int i = 0; i < deduplicatedIds.size(); i++) {
                Long fileId = deduplicatedIds.get(i);
                if (fileId == null) {
                    continue;
                }

                FileResource fileResource = fileResourceMapper.selectById(fileId);
                if (fileResource == null || Boolean.TRUE.equals(fileResource.getIsDeleted())) {
                    throw new BusinessException("附件文件不存在或已删除");
                }
                if (!Objects.equals(fileResource.getUserId(), userId)) {
                    throw new BusinessException("附件文件不属于当前用户");
                }

                TeamPostAttachment relation = new TeamPostAttachment();
                relation.setPostId(post.getPostId());
                relation.setFileId(fileId);
                relation.setSortOrder(i);
                relation.setCreatedTime(now);
                teamPostAttachmentMapper.insert(relation);

                fileResource.setTargetType(FILE_TARGET_TYPE_TEAM_POST_ATTACHMENT);
                fileResource.setTargetId(post.getPostId());
                fileResource.setIsTemp(false);
                fileResource.setUpdateTime(now);
                fileResourceMapper.updateById(fileResource);
            }
        }

        return post.getPostId();
    }

    @Override
    public TeamPostListResponse getTeamPosts(Integer projectId, Integer userId, Integer page, Integer size) {
        Project project = validateProjectExists(projectId);
        ensureTeamMember(project, userId);

        long current = page == null || page < 1 ? 1L : page;
        long pageSize = size == null || size < 1 ? 10L : size;

        LambdaQueryWrapper<TeamPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(TeamPost::getProjectId, projectId)
                .eq(TeamPost::getStatus, TEAM_POST_STATUS_NORMAL)
                .orderByDesc(TeamPost::getCreatedTime);

        Page<TeamPost> pageParam = new Page<>(current, pageSize);
        Page<TeamPost> postPage = teamPostMapper.selectPage(pageParam, postWrapper);
        List<TeamPost> posts = postPage.getRecords();

        TeamPostListResponse response = new TeamPostListResponse();
        response.setTotal(postPage.getTotal());
        response.setPage(Math.toIntExact(current));
        response.setSize(Math.toIntExact(pageSize));
        if (posts == null || posts.isEmpty()) {
            response.setList(new ArrayList<>());
            return response;
        }

        List<Integer> postIds = posts.stream()
                .map(TeamPost::getPostId)
                .collect(Collectors.toList());
        List<Integer> userIds = posts.stream()
                .map(TeamPost::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }

        Map<Long, String> avatarUrlMap = buildAvatarUrlMap(userMap.values());

        Map<Integer, List<Long>> postAttachmentFileIds = new HashMap<>();
        if (!postIds.isEmpty()) {
            LambdaQueryWrapper<TeamPostAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
            attachmentWrapper.in(TeamPostAttachment::getPostId, postIds)
                    .orderByAsc(TeamPostAttachment::getSortOrder, TeamPostAttachment::getRelationId);
            List<TeamPostAttachment> attachments = teamPostAttachmentMapper.selectList(attachmentWrapper);
            for (TeamPostAttachment attachment : attachments) {
                postAttachmentFileIds
                        .computeIfAbsent(attachment.getPostId(), key -> new ArrayList<>())
                        .add(attachment.getFileId());
            }
        }

        Set<Long> allAttachmentIds = new HashSet<>();
        for (List<Long> fileIds : postAttachmentFileIds.values()) {
            allAttachmentIds.addAll(fileIds);
        }
        Map<Long, String> fileUrlMap = new HashMap<>();
        if (!allAttachmentIds.isEmpty()) {
            List<FileResource> fileResources = fileResourceMapper.selectBatchIds(allAttachmentIds);
            for (FileResource fileResource : fileResources) {
                fileUrlMap.put(fileResource.getFileId(), fileResource.getFileUrl());
            }
        }

        List<TeamPostListResponse.PostItem> postItems = new ArrayList<>();
        for (TeamPost post : posts) {
            TeamPostListResponse.PostItem item = new TeamPostListResponse.PostItem();
            item.setPostId(post.getPostId());
            item.setProjectId(post.getProjectId());
            item.setTitle(post.getTitle());
            item.setContent(post.getContent());
            item.setUserId(post.getUserId());
            item.setCreatedTime(post.getCreatedTime());

            User user = userMap.get(post.getUserId());
            if (user != null) {
                item.setNickname(user.getNickname());
                item.setAvatar(user.getAvatarFileId() == null ? null : avatarUrlMap.get(user.getAvatarFileId()));
            }

            List<Long> fileIds = postAttachmentFileIds.getOrDefault(post.getPostId(), Collections.emptyList());
            List<String> attachmentUrls = fileIds.stream()
                    .map(fileUrlMap::get)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            item.setAttachments(attachmentUrls);
            postItems.add(item);
        }

        response.setList(postItems);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskResponse createTask(Integer projectId, Integer userId, CreateTaskRequest request) {
        Project project = validateProjectExists(projectId);
        ensureCaptain(project, userId);

        if (!isTeamMember(projectId, request.getAssigneeId(), project.getPublisherUserId())) {
            throw new BusinessException("负责人必须是团队成员");
        }

        LocalDateTime now = LocalDateTime.now();
        TeamTask task = new TeamTask();
        task.setProjectId(projectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(userId);
        task.setDeadline(request.getDeadline());
        task.setStatus(TEAM_TASK_TODO);
        task.setCreatedTime(now);
        task.setUpdateTime(now);
        teamTaskMapper.insert(task);

        return toTaskResponse(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskResponse updateTaskStatus(Integer taskId, Integer userId, UpdateTaskStatusRequest request) {
        TeamTask task = teamTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("任务不存在");
        }

        Project project = validateProjectExists(task.getProjectId());
        boolean captain = isCaptain(project, userId);
        boolean assignee = Objects.equals(task.getAssigneeId(), userId);
        if (!captain && !assignee) {
            throw new BusinessException("无权限更新该任务状态");
        }

        task.setStatus(request.getStatus());
        task.setUpdateTime(LocalDateTime.now());
        teamTaskMapper.updateById(task);
        return toTaskResponse(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Integer projectId, Integer captainId, Integer targetUserId) {
        Project project = validateProjectExists(projectId);
        ensureCaptain(project, captainId);

        if (Objects.equals(project.getPublisherUserId(), targetUserId)) {
            throw new BusinessException("不能移除队长");
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getUserId, targetUserId)
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode());
        TeamMember member = teamMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("该成员不在团队中");
        }

        member.setStatus(TeamMemberStatusEnum.REMOVED.getCode());
        member.setUpdateTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitTeam(Integer projectId, Integer userId) {
        Project project = validateProjectExists(projectId);
        if (isCaptain(project, userId)) {
            throw new BusinessException("队长暂不支持退出团队，请先转让队长身份");
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode());
        TeamMember member = teamMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("您当前不在该团队中");
        }

        member.setStatus(TeamMemberStatusEnum.QUIT.getCode());
        member.setUpdateTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }

    private MyTeamListResponse.TeamBriefItem buildTeamBrief(Project project, String role, boolean includeUnread, Integer userId) {
        MyTeamListResponse.TeamBriefItem item = new MyTeamListResponse.TeamBriefItem();
        item.setProjectId(project.getProjectId());
        item.setName(project.getName());
        item.setStatus(project.getStatus());
        item.setMemberCount(countTeamMembers(project.getProjectId(), project.getPublisherUserId()));
        item.setRole(role);
        if (includeUnread) {
            item.setUnreadCount(calcProjectUnreadCount(project.getProjectId(), userId));
        }
        return item;
    }

    private TeamDetailResponse.TaskStats buildTaskStats(Integer projectId) {
        LambdaQueryWrapper<TeamTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(TeamTask::getProjectId, projectId);
        List<TeamTask> tasks = teamTaskMapper.selectList(taskWrapper);

        int total = tasks.size();
        int completed = 0;
        int inProgress = 0;
        for (TeamTask task : tasks) {
            if (Objects.equals(task.getStatus(), TEAM_TASK_COMPLETED)) {
                completed++;
            } else if (Objects.equals(task.getStatus(), TEAM_TASK_IN_PROGRESS)) {
                inProgress++;
            }
        }

        TeamDetailResponse.TaskStats taskStats = new TeamDetailResponse.TaskStats();
        taskStats.setTotal(total);
        taskStats.setCompleted(completed);
        taskStats.setInProgress(inProgress);
        return taskStats;
    }

    private List<TeamMemberResponse> buildTeamMembers(Project project) {
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getProjectId, project.getProjectId())
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode())
                .orderByAsc(TeamMember::getJoinTime);
        List<TeamMember> activeMembers = teamMemberMapper.selectList(memberWrapper);

        Set<Integer> userIds = new LinkedHashSet<>();
        userIds.add(project.getPublisherUserId());
        for (TeamMember member : activeMembers) {
            userIds.add(member.getUserId());
        }

        Map<Integer, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }
        Map<Long, String> avatarUrlMap = buildAvatarUrlMap(userMap.values());

        List<TeamMemberResponse> responses = new ArrayList<>();

        User captain = userMap.get(project.getPublisherUserId());
        if (captain == null) {
            captain = userMapper.selectById(project.getPublisherUserId());
        }

        TeamMemberResponse captainResponse = new TeamMemberResponse();
        captainResponse.setUserId(project.getPublisherUserId());
        captainResponse.setNickname(captain != null ? captain.getNickname() : null);
        captainResponse.setAvatar(resolveAvatar(captain, avatarUrlMap));
        captainResponse.setRole("队长");
        captainResponse.setTeamRole("队长");
        captainResponse.setJoinTime(formatDate(project.getReleaseTime()));
        captainResponse.setStatus(TeamMemberStatusEnum.IN_TEAM.getCode());
        responses.add(captainResponse);

        for (TeamMember member : activeMembers) {
            if (Objects.equals(member.getUserId(), project.getPublisherUserId())) {
                continue;
            }
            User user = userMap.get(member.getUserId());
            TeamMemberResponse response = new TeamMemberResponse();
            response.setUserId(member.getUserId());
            response.setNickname(user != null ? user.getNickname() : null);
            response.setAvatar(resolveAvatar(user, avatarUrlMap));
            response.setRole("队员");
            response.setTeamRole(StringUtils.hasText(member.getRole()) ? member.getRole() : "队员");
            response.setJoinTime(formatDate(member.getJoinTime()));
            response.setStatus(member.getStatus());
            responses.add(response);
        }

        return responses;
    }

    private Map<Long, String> buildAvatarUrlMap(Iterable<User> users) {
        Set<Long> avatarFileIds = new HashSet<>();
        for (User user : users) {
            if (user != null && user.getAvatarFileId() != null) {
                avatarFileIds.add(user.getAvatarFileId());
            }
        }
        if (avatarFileIds.isEmpty()) {
            return new HashMap<>();
        }

        List<FileResource> fileResources = fileResourceMapper.selectBatchIds(avatarFileIds);
        Map<Long, String> avatarUrlMap = new HashMap<>();
        for (FileResource fileResource : fileResources) {
            avatarUrlMap.put(fileResource.getFileId(), fileResource.getFileUrl());
        }
        return avatarUrlMap;
    }

    private String resolveAvatar(User user, Map<Long, String> avatarUrlMap) {
        if (user == null || user.getAvatarFileId() == null) {
            return null;
        }
        return avatarUrlMap.get(user.getAvatarFileId());
    }

    private TaskResponse toTaskResponse(TeamTask task) {
        TaskResponse response = new TaskResponse();
        response.setTaskId(task.getTaskId());
        response.setProjectId(task.getProjectId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setAssigneeId(task.getAssigneeId());
        response.setCreatorId(task.getCreatorId());
        response.setDeadline(task.getDeadline());
        response.setStatus(task.getStatus());
        response.setCreatedTime(task.getCreatedTime());

        User assignee = userMapper.selectById(task.getAssigneeId());
        if (assignee != null) {
            response.setAssigneeNickname(assignee.getNickname());
        }
        return response;
    }

    private String resolveProgress(Project project) {
        if (StringUtils.hasText(project.getProjectProgress())) {
            return project.getProjectProgress();
        }
        return project.getProjectIntro();
    }

    private Project validateProjectExists(Integer projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }
        return project;
    }

    private void ensureCaptain(Project project, Integer userId) {
        if (!isCaptain(project, userId)) {
            throw new BusinessException("仅队长可执行该操作");
        }
    }

    private void ensureTeamMember(Project project, Integer userId) {
        if (isCaptain(project, userId)) {
            return;
        }
        if (!isTeamMember(project.getProjectId(), userId, project.getPublisherUserId())) {
            throw new BusinessException("您不是该团队成员");
        }
    }

    private boolean isCaptain(Project project, Integer userId) {
        return Objects.equals(project.getPublisherUserId(), userId);
    }

    private boolean isTeamMember(Integer projectId, Integer userId, Integer captainId) {
        if (Objects.equals(userId, captainId)) {
            return true;
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode());
        Long count = teamMemberMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private Integer countTeamMembers(Integer projectId, Integer captainId) {
        Set<Integer> userIds = new HashSet<>();
        if (captainId != null) {
            userIds.add(captainId);
        }

        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getStatus, TeamMemberStatusEnum.IN_TEAM.getCode());
        List<TeamMember> members = teamMemberMapper.selectList(memberWrapper);
        for (TeamMember member : members) {
            userIds.add(member.getUserId());
        }
        return userIds.size();
    }

    private Integer calcProjectUnreadCount(Integer projectId, Integer userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getProjectId, projectId)
                .eq(ChatSession::getStatus, CHAT_SESSION_STATUS_NORMAL)
                .and(w -> w.eq(ChatSession::getUser1Id, userId)
                        .or()
                        .eq(ChatSession::getUser2Id, userId));
        List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);

        int unreadCount = 0;
        for (ChatSession session : sessions) {
            if (Objects.equals(session.getUser1Id(), userId)) {
                unreadCount += session.getUser1Unread() == null ? 0 : session.getUser1Unread();
            } else if (Objects.equals(session.getUser2Id(), userId)) {
                unreadCount += session.getUser2Unread() == null ? 0 : session.getUser2Unread();
            }
        }
        return unreadCount;
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
