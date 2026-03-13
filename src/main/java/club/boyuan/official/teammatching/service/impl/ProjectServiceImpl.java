package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.enums.ApplicationResultEnum;
import club.boyuan.official.teammatching.common.enums.ProjectStatusEnum;
import club.boyuan.official.teammatching.dto.request.project.ApplyProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.ProjectQueryRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.response.project.ProjectCardResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
import club.boyuan.official.teammatching.dto.response.project.ApplyProjectResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectListResponse;
import club.boyuan.official.teammatching.entity.ChatSession;
import club.boyuan.official.teammatching.entity.Favorite;
import club.boyuan.official.teammatching.entity.Follow;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.ProjectRoleRequirements;
import club.boyuan.official.teammatching.entity.TeamApplication;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.ProjectRoleRequirementMapper;
import club.boyuan.official.teammatching.mapper.ChatSessionMapper;
import club.boyuan.official.teammatching.mapper.TeamApplicationMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.mapper.FavoriteMapper;
import club.boyuan.official.teammatching.mapper.FollowMapper;
import club.boyuan.official.teammatching.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectMapper projectMapper;
    private final ProjectRoleRequirementMapper roleRequirementMapper;
    private final TeamApplicationMapper teamApplicationMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createProject(Integer userId, CreateProjectRequest request) {
        log.info("创建项目，userId: {}, 项目名称：{}", userId, request.getName());
        
        // 1. 创建项目
        Project project = new Project();
        project.setName(request.getName());
        project.setBelongTrack(request.getBelongTrack());
        project.setLevel(request.getLevel());
        project.setProjectType(request.getProjectType());
        project.setProjectIntro(request.getProjectIntro());
        project.setProjectFeatures(request.getProjectFeatures());
        project.setTags(request.getTags());
        project.setAllowCrossMajorApplication(request.getAllowCrossMajor());
        project.setPublisherUserId(userId);
        project.setIsAnonymous(request.getIsAnonymous());
        project.setContactInfo(request.getContactInfo());
        project.setDeadlineRecruit(request.getDeadlineRecruit());
        project.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        project.setReleaseTime(LocalDateTime.now());
        project.setViewCount(0);
        project.setFavoriteCount(0);
        project.setApplyCount(0);
        project.setAuditStatus(0); // 待审核
        
        projectMapper.insert(project);
        
        // 2. 创建角色要求
        if (request.getRoleRequirements() != null && !request.getRoleRequirements().isEmpty()) {
            for (CreateProjectRequest.RoleRequirementDTO roleReq : request.getRoleRequirements()) {
                ProjectRoleRequirements requirement = new ProjectRoleRequirements();
                requirement.setProjectId(project.getProjectId());
                requirement.setRole(roleReq.getRole());
                requirement.setMemberQuota(roleReq.getMemberQuota());
                requirement.setRecruitRequirements(roleReq.getRecruitRequirements());
                requirement.setCurrentApplicants(0);
                requirement.setCurrentMembers(0);
                requirement.setCreatedTime(LocalDateTime.now());
                
                roleRequirementMapper.insert(requirement);
            }
        }
        
        log.info("项目创建成功，projectId: {}", project.getProjectId());
        return project.getProjectId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(Integer projectId, Integer userId, UpdateProjectRequest request) {
        log.info("更新项目，projectId: {}, userId: {}", projectId, userId);
        
        // 1. 查询项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }
        
        // 2. 验证权限（只有发布者可以修改）
        if (!project.getPublisherUserId().equals(userId)) {
            throw new RuntimeException("无权限修改该项目");
        }
        
        // 3. 更新项目信息
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getBelongTrack() != null) {
            project.setBelongTrack(request.getBelongTrack());
        }
        if (request.getLevel() != null) {
            project.setLevel(request.getLevel());
        }
        if (request.getProjectType() != null) {
            project.setProjectType(request.getProjectType());
        }
        if (request.getProjectIntro() != null) {
            project.setProjectIntro(request.getProjectIntro());
        }
        if (request.getProjectFeatures() != null) {
            project.setProjectFeatures(request.getProjectFeatures());
        }
        if (request.getTags() != null) {
            project.setTags(request.getTags());
        }
        if (request.getAllowCrossMajor() != null) {
            project.setAllowCrossMajorApplication(request.getAllowCrossMajor());
        }
        if (request.getIsAnonymous() != null) {
            project.setIsAnonymous(request.getIsAnonymous());
        }
        if (request.getContactInfo() != null) {
            project.setContactInfo(request.getContactInfo());
        }
        if (request.getDeadlineRecruit() != null) {
            project.setDeadlineRecruit(request.getDeadlineRecruit());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        
        // 4. 更新角色要求（如果需要）
        if (request.getRoleRequirements() != null) {
            // 先删除旧的角色要求
            LambdaQueryWrapper<ProjectRoleRequirements> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProjectRoleRequirements::getProjectId, projectId);
            roleRequirementMapper.delete(queryWrapper);
            
            // 再插入新的角色要求
            for (UpdateProjectRequest.RoleRequirementDTO roleReq : request.getRoleRequirements()) {
                ProjectRoleRequirements requirement = new ProjectRoleRequirements();
                requirement.setProjectId(projectId);
                requirement.setRole(roleReq.getRole());
                requirement.setMemberQuota(roleReq.getMemberQuota());
                requirement.setRecruitRequirements(roleReq.getRecruitRequirements());
                requirement.setCurrentApplicants(0);
                requirement.setCurrentMembers(0);
                requirement.setCreatedTime(LocalDateTime.now());
                
                roleRequirementMapper.insert(requirement);
            }
        }
        
        log.info("项目更新成功，projectId: {}", projectId);
    }
    
    @Override
    public ProjectDetailResponse getProjectDetail(Integer projectId, Integer currentUserId) {
        log.info("获取项目详情，projectId: {}, currentUserId: {}", projectId, currentUserId);
        
        // 1. 查询项目
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }
        
        // 2. 增加浏览次数
        project.setViewCount(project.getViewCount() + 1);
        projectMapper.updateById(project);
        
        // 3. 转换为响应 DTO
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setProjectId(project.getProjectId());
        response.setName(project.getName());
        response.setBelongTrack(project.getBelongTrack());
        response.setLevel(project.getLevel());
        response.setProjectType(project.getProjectType());
        response.setProjectIntro(project.getProjectIntro());
        response.setProjectFeatures(project.getProjectFeatures());
        response.setTags(project.getTags());
        response.setAllowCrossMajor(project.getAllowCrossMajorApplication());
        response.setDeadlineRecruit(project.getDeadlineRecruit());
        response.setStatus(project.getStatus());
        response.setAuditStatus(project.getAuditStatus());
        response.setViewCount(project.getViewCount());
        response.setFavoriteCount(project.getFavoriteCount());
        response.setApplyCount(project.getApplyCount());
        response.setReleaseTime(project.getReleaseTime());
        
        // 4. 设置发布人信息
        if (project.getIsAnonymous()) {
            // 匿名发布，显示临时联系方式
            ProjectDetailResponse.PublisherInfo publisherInfo = new ProjectDetailResponse.PublisherInfo();
            publisherInfo.setUserId(null);
            publisherInfo.setNickname("匿名用户");
            publisherInfo.setAvatar(null);
            response.setPublisherInfo(publisherInfo);
        } else {
            // 非匿名，显示真实发布者信息
            User publisher = userMapper.selectById(project.getPublisherUserId());
            if (publisher != null) {
                ProjectDetailResponse.PublisherInfo publisherInfo = new ProjectDetailResponse.PublisherInfo();
                publisherInfo.setUserId(publisher.getUserId());
                publisherInfo.setNickname(publisher.getNickname());
                publisherInfo.setAvatar(null); // User 实体使用 avatarFileId，暂时返回 null
                response.setPublisherInfo(publisherInfo);
            }
        }
        
        // 5. 查询角色要求
        LambdaQueryWrapper<ProjectRoleRequirements> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectRoleRequirements::getProjectId, projectId);
        List<ProjectRoleRequirements> requirements = roleRequirementMapper.selectList(queryWrapper);
        
        List<ProjectDetailResponse.ProjectRoleInfo> roleResponses = requirements.stream()
            .map(req -> {
                ProjectDetailResponse.ProjectRoleInfo roleResponse = new ProjectDetailResponse.ProjectRoleInfo();
                roleResponse.setRequirementId(req.getRequirementId());
                roleResponse.setRole(req.getRole());
                roleResponse.setMemberQuota(req.getMemberQuota());
                roleResponse.setCurrentApplicants(req.getCurrentApplicants());
                roleResponse.setCurrentMembers(req.getCurrentMembers());
                roleResponse.setRecruitRequirements(req.getRecruitRequirements());
                return roleResponse;
            })
            .collect(Collectors.toList());
        
        response.setRoleRequirements(roleResponses);
        
        return response;
    }
    
    @Override
    public List<ProjectCardResponse> getMyPublishedProjects(Integer userId,
                                                            Integer status,
                                                            Integer auditStatus,
                                                            Integer page,
                                                            Integer size) {
        log.info("获取我发布的项目列表，userId: {}, status: {}, auditStatus: {}, page: {}, size: {}",
                userId, status, auditStatus, page, size);

        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getPublisherUserId, userId);

        if (status != null) {
            queryWrapper.eq(Project::getStatus, status);
        }
        if (auditStatus != null) {
            queryWrapper.eq(Project::getAuditStatus, auditStatus);
        }

        queryWrapper.orderByDesc(Project::getReleaseTime);

        long current = Optional.ofNullable(page).map(Integer::longValue).orElse(1L);
        long pageSize = Optional.ofNullable(size).map(Integer::longValue).orElse(10L);

        Page<Project> pageParam = new Page<>(current, pageSize);
        Page<Project> projectPage = projectMapper.selectPage(pageParam, queryWrapper);
        List<Project> projects = projectPage.getRecords();

        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        // 统计角色数、已填满的角色数
        List<Integer> projectIds = projects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProjectRoleRequirements> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.in(ProjectRoleRequirements::getProjectId, projectIds);
        List<ProjectRoleRequirements> allRequirements = roleRequirementMapper.selectList(roleQuery);

        Map<Integer, List<ProjectRoleRequirements>> requirementsByProject = allRequirements.stream()
                .collect(Collectors.groupingBy(ProjectRoleRequirements::getProjectId));

        List<ProjectCardResponse> result = new ArrayList<>();
        for (Project project : projects) {
            ProjectCardResponse card = new ProjectCardResponse();
            card.setProjectId(project.getProjectId());
            card.setName(project.getName());
            card.setBelongTrack(project.getBelongTrack());
            card.setStatus(project.getStatus());
            card.setAuditStatus(project.getAuditStatus());
            card.setReleaseTime(project.getReleaseTime());
            card.setViewCount(project.getViewCount());
            card.setApplyCount(project.getApplyCount());

            List<ProjectRoleRequirements> requirementList =
                    requirementsByProject.getOrDefault(project.getProjectId(), Collections.emptyList());

            card.setTotalRoles(requirementList.size());

            long filledCount = requirementList.stream()
                    .filter(r -> r.getCurrentMembers() != null
                            && r.getMemberQuota() != null
                            && r.getCurrentMembers() >= r.getMemberQuota())
                    .count();
            card.setFilledRoles((int) filledCount);

            result.add(card);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplyProjectResponse applyProject(Integer projectId, Integer userId, ApplyProjectRequest request) {
        log.info("申请加入项目，projectId: {}, userId: {}, requirementId: {}",
                projectId, userId, request.getRequirementId());

        // 1. 校验项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }

        // 2. 校验角色要求是否存在且属于该项目
        ProjectRoleRequirements requirement = roleRequirementMapper.selectById(request.getRequirementId());
        if (requirement == null || !Objects.equals(requirement.getProjectId(), projectId)) {
            throw new BusinessException("角色要求不存在或不属于该项目");
        }

        // 3. 校验是否已截止
        if (project.getDeadlineRecruit() != null
                && project.getDeadlineRecruit().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该项目招募已截止");
        }

        // 4. 检查是否已投递过该项目同一角色
        LambdaQueryWrapper<TeamApplication> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(TeamApplication::getApplicantUserId, userId)
                .eq(TeamApplication::getProjectId, projectId)
                .eq(TeamApplication::getRequirementId, request.getRequirementId());
        Long existCount = teamApplicationMapper.selectCount(existWrapper);
        if (existCount != null && existCount > 0) {
            throw new BusinessException("您已投递过该项目的该角色，请勿重复申请");
        }

        // 5. 创建申请记录
        TeamApplication application = new TeamApplication();
        application.setApplicantUserId(userId);
        application.setProjectId(projectId);
        application.setRequirementId(request.getRequirementId());
        application.setRole(requirement.getRole());
        application.setApplyReason(request.getApplyReason());
        application.setCustomResumeFileId(request.getCustomResumeFileId());
        application.setApplicationAttachmentFileId(request.getApplicationAttachmentFileId());
        application.setResult(ApplicationResultEnum.PENDING.getCode());
        LocalDateTime now = LocalDateTime.now();
        application.setApplyTime(now);
        application.setUpdateTime(now);

        teamApplicationMapper.insert(application);

        // 6. 更新角色当前申请人数
        requirement.setCurrentApplicants(
                (requirement.getCurrentApplicants() == null ? 0 : requirement.getCurrentApplicants()) + 1);
        requirement.setUpdateTime(now);
        roleRequirementMapper.updateById(requirement);

        // 7. 创建或获取会话
        Integer publisherUserId = project.getPublisherUserId();
        if (publisherUserId == null) {
            throw new BusinessException("项目发布人信息异常");
        }

        // 为避免重复创建会话，检查是否已存在双方在该项目下的会话
        LambdaQueryWrapper<ChatSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(ChatSession::getProjectId, projectId)
                .and(w -> w
                        .and(w1 -> w1.eq(ChatSession::getUser1Id, userId)
                                .eq(ChatSession::getUser2Id, publisherUserId))
                        .or()
                        .and(w2 -> w2.eq(ChatSession::getUser1Id, publisherUserId)
                                .eq(ChatSession::getUser2Id, userId)));

        ChatSession session = chatSessionMapper.selectOne(sessionWrapper);
        if (session == null) {
            session = new ChatSession();
            session.setUser1Id(userId);
            session.setUser2Id(publisherUserId);
            session.setProjectId(projectId);
            session.setLastMessage(null);
            session.setLastMsgTime(now);
            session.setUser1Unread(0);
            session.setUser2Unread(0);
            session.setStatus(1);
            session.setUpdateTime(now);
            chatSessionMapper.insert(session);
        }

        log.info("申请加入项目成功，applicationId: {}, sessionId: {}",
                application.getApplicationId(), session.getSessionId());

        ApplyProjectResponse response = new ApplyProjectResponse();
        response.setApplicationId(application.getApplicationId());
        response.setMessage("投递成功，等待队长回复");
        response.setSessionId(session.getSessionId());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProjectStatus(Integer projectId, Integer userId, Integer status) {
        log.info("更新项目状态，projectId: {}, userId: {}, status: {}", projectId, userId, status);

        if (!ProjectStatusEnum.isValidStatus(status)) {
            throw new BusinessException("非法的项目状态值");
        }

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }

        if (!project.getPublisherUserId().equals(userId)) {
            throw new BusinessException("无权限修改该项目状态");
        }

        project.setStatus(status);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
    }

    @Override
    public Map<String, Object> getProjectStats(Integer projectId) {
        log.info("获取项目统计数据，projectId: {}", projectId);

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("viewCount", project.getViewCount());
        result.put("favoriteCount", project.getFavoriteCount());
        result.put("applyCount", project.getApplyCount());

        // 申请趋势，这里先返回空列表，后续可按日期统计 team_application 表
        result.put("applicationTrend", new ArrayList<>());

        return result;
    }

    @Override
    public List<ProjectListResponse> getProjectList(ProjectQueryRequest request) {
        log.info("获取项目列表，request: {}", request);

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        // 只展示正在招募中的项目：可根据业务需要调整
        wrapper.eq(Project::getStatus, ProjectStatusEnum.RECRUITING.getCode());

        if (request.getTrack() != null && !request.getTrack().isBlank()) {
            wrapper.eq(Project::getBelongTrack, request.getTrack());
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Project::getName, request.getKeyword())
                    .or().like(Project::getProjectIntro, request.getKeyword()));
        }

        // 排序
        String sort = request.getSort();
        if ("hot".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Project::getViewCount, Project::getFavoriteCount);
        } else if ("deadline".equalsIgnoreCase(sort)) {
            wrapper.orderByAsc(Project::getDeadlineRecruit);
        } else {
            // 默认 latest
            wrapper.orderByDesc(Project::getReleaseTime);
        }

        long current = Optional.ofNullable(request.getPage()).map(Integer::longValue).orElse(1L);
        long pageSize = Optional.ofNullable(request.getSize()).map(Integer::longValue).orElse(10L);

        Page<Project> pageParam = new Page<>(current, pageSize);
        Page<Project> projectPage = projectMapper.selectPage(pageParam, wrapper);
        List<Project> projects = projectPage.getRecords();

        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        return buildProjectListResponses(projects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavoriteProject(Integer projectId, Integer userId) {
        log.info("切换项目收藏状态，projectId: {}, userId: {}", projectId, userId);

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, 1)
                .eq(Favorite::getTargetId, projectId);

        Favorite exist = favoriteMapper.selectOne(wrapper);
        LocalDateTime now = LocalDateTime.now();

        boolean isFavored;
        if (exist == null) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setTargetType(1);
            favorite.setTargetId(projectId);
            favorite.setCreatedTime(now);
            favoriteMapper.insert(favorite);

            project.setFavoriteCount(
                    (project.getFavoriteCount() == null ? 0 : project.getFavoriteCount()) + 1);
            isFavored = true;
        } else {
            favoriteMapper.deleteById(exist.getFavoriteId());
            int current = project.getFavoriteCount() == null ? 0 : project.getFavoriteCount();
            project.setFavoriteCount(Math.max(0, current - 1));
            isFavored = false;
        }

        project.setUpdateTime(now);
        projectMapper.updateById(project);

        return isFavored;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollowUser(Integer targetUserId, Integer userId) {
        log.info("切换用户关注状态，targetUserId: {}, userId: {}", targetUserId, userId);

        if (Objects.equals(targetUserId, userId)) {
            throw new BusinessException("不能关注自己");
        }

        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId)
                .eq(Follow::getFollowingId, targetUserId);

        Follow exist = followMapper.selectOne(wrapper);
        boolean isFollowed;

        if (exist == null) {
            Follow follow = new Follow();
            follow.setFollowerId(userId);
            follow.setFollowingId(targetUserId);
            follow.setCreatedTime(LocalDateTime.now());
            followMapper.insert(follow);
            isFollowed = true;
        } else {
            followMapper.deleteById(exist.getFollowId());
            isFollowed = false;
        }

        return isFollowed;
    }

    @Override
    public List<ProjectListResponse> getSimilarProjects(Integer projectId) {
        log.info("获取相似项目，projectId: {}", projectId);

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("项目不存在");
        }

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Project::getProjectId, projectId);
        wrapper.eq(Project::getBelongTrack, project.getBelongTrack());
        wrapper.eq(Project::getStatus, ProjectStatusEnum.RECRUITING.getCode());
        wrapper.orderByDesc(Project::getViewCount);

        List<Project> projects = projectMapper.selectList(wrapper);
        return buildProjectListResponses(projects);
    }

    @Override
    public List<ProjectListResponse> getMatchedProjects(Integer userId) {
        log.info("获取智能匹配项目，userId: {}", userId);

        // 当前实现：先简单按热度推荐正在招募中的项目，后续可接入更复杂的推荐算法
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, ProjectStatusEnum.RECRUITING.getCode());
        wrapper.orderByDesc(Project::getViewCount, Project::getFavoriteCount);

        List<Project> projects = projectMapper.selectList(wrapper);
        return buildProjectListResponses(projects);
    }

    private List<ProjectListResponse> buildProjectListResponses(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询发布人信息
        List<Integer> publisherIds = projects.stream()
                .map(Project::getPublisherUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = new HashMap<>();
        if (!publisherIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(publisherIds);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));
        }

        // 查询角色要求
        List<Integer> projectIds = projects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProjectRoleRequirements> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(ProjectRoleRequirements::getProjectId, projectIds);
        List<ProjectRoleRequirements> allRequirements = roleRequirementMapper.selectList(roleWrapper);

        Map<Integer, List<ProjectRoleRequirements>> requirementsByProject = allRequirements.stream()
                .collect(Collectors.groupingBy(ProjectRoleRequirements::getProjectId));

        List<ProjectListResponse> result = new ArrayList<>();

        for (Project project : projects) {
            ProjectListResponse item = new ProjectListResponse();
            item.setProjectId(project.getProjectId());
            item.setName(project.getName());
            item.setBelongTrack(project.getBelongTrack());
            item.setProjectIntro(project.getProjectIntro());
            item.setDeadlineRecruit(project.getDeadlineRecruit());
            item.setStatus(project.getStatus());
            item.setViewCount(project.getViewCount());
            item.setFavoriteCount(project.getFavoriteCount());

            // 发布人信息
            User publisher = userMap.get(project.getPublisherUserId());
            ProjectListResponse.PublisherInfo publisherInfo = new ProjectListResponse.PublisherInfo();
            if (publisher != null) {
                publisherInfo.setNickname(publisher.getNickname());
                publisherInfo.setAvatar(
                        publisher.getAvatarFileId() != null ? publisher.getAvatarFileId().toString() : null
                );
            } else {
                publisherInfo.setNickname("");
                publisherInfo.setAvatar("");
            }
            item.setPublisherInfo(publisherInfo);

            // 角色汇总
            List<ProjectRoleRequirements> requirementList =
                    requirementsByProject.getOrDefault(project.getProjectId(), Collections.emptyList());

            List<ProjectListResponse.RoleSummary> roleSummaries = requirementList.stream()
                    .map(req -> {
                        ProjectListResponse.RoleSummary summary = new ProjectListResponse.RoleSummary();
                        summary.setRole(req.getRole());
                        summary.setMemberQuota(req.getMemberQuota());
                        summary.setCurrentMembers(req.getCurrentMembers());
                        return summary;
                    })
                    .collect(Collectors.toList());

            item.setRoleSummaries(roleSummaries);

            result.add(item);
        }

        return result;
    }
}