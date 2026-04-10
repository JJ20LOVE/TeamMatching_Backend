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
import club.boyuan.official.teammatching.mapper.SkillTagMapper;
import club.boyuan.official.teammatching.mapper.UserSkillRelationMapper;
import club.boyuan.official.teammatching.mq.producer.NotificationProducer;
import club.boyuan.official.teammatching.mq.support.NotificationPreferenceUtils;
import club.boyuan.official.teammatching.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
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
    private final UserSkillRelationMapper userSkillRelationMapper;
    private final SkillTagMapper skillTagMapper;
    private final NotificationProducer notificationProducer;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "projectMatch", allEntries = true)
    public Integer createProject(Integer userId, CreateProjectRequest request) {
        log.info("创建项目，userId: {}, 项目名称：{}", userId, request.getName());
        
        // 1. 创建项目
        Project project = new Project();
        project.setName(request.getName());
        project.setBelongTrack(request.getBelongTrack());
        project.setLevel(request.getLevel());
        project.setProjectType(request.getProjectType());
        project.setProjectIntro(request.getProjectIntro());
        project.setProjectProgress(request.getProjectProgress());
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
        project.setAuditStatus(1); // TODO: 后续完善审核逻辑
        
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
    @CacheEvict(cacheNames = "projectMatch", allEntries = true)
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
    public List<ProjectCardResponse> getPublishedProjectsByUserId(Integer userId,
                                                                  Integer status,
                                                                  Integer auditStatus,
                                                                  Integer page,
                                                                  Integer size) {
        // 当前逻辑与“我发布的项目”一致，只是 userId 来自 path 参数且不要求必须是当前登录用户。
        return getMyPublishedProjects(userId, status, auditStatus, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "projectMatch", allEntries = true)
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

        User publisher = userMapper.selectById(publisherUserId);
        if (publisher != null && NotificationPreferenceUtils.isChannelEnabled(publisher.getProjectUpdateNotify())) {
            User applicant = userMapper.selectById(userId);
            String applicantName = applicant != null && StringUtils.hasText(applicant.getNickname())
                    ? applicant.getNickname() : "有人";
            notificationProducer.publishProjectUpdate(
                    publisherUserId,
                    "新的项目申请",
                    applicantName + " 申请加入「" + project.getName() + "」",
                    "team_application",
                    String.valueOf(application.getApplicationId()));
        }

        ApplyProjectResponse response = new ApplyProjectResponse();
        response.setApplicationId(application.getApplicationId());
        response.setMessage("投递成功，等待队长回复");
        response.setSessionId(session.getSessionId());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "projectMatch", allEntries = true)
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

        // 只展示正在招募中的项目
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
    @Cacheable(cacheNames = "projectMatch", key = "#userId")
    public List<ProjectListResponse> getMatchedProjects(Integer userId) {
        log.info("获取智能匹配项目，userId: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 1) 构建用户画像：技能词 + 兴趣赛道偏好
        Map<String, Double> userSkillWeights = buildUserSkillWeights(userId, user.getTechStack());
        Map<String, Double> trackPreference = buildUserTrackPreference(userId);

        // 2) 候选集：优先取“审核通过 + 招募中 + 未截止”的项目；不足则降级为“招募中 + 未截止”
        List<Project> candidates = queryMatchedCandidates(true);
        if (candidates.size() < 30) {
            candidates = queryMatchedCandidates(false);
        }
        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 3) 批量取角色要求
        List<Integer> projectIds = candidates.stream().map(Project::getProjectId).collect(Collectors.toList());
        LambdaQueryWrapper<ProjectRoleRequirements> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(ProjectRoleRequirements::getProjectId, projectIds);
        List<ProjectRoleRequirements> allRequirements = roleRequirementMapper.selectList(roleWrapper);
        Map<Integer, List<ProjectRoleRequirements>> requirementsByProject = allRequirements.stream()
                .collect(Collectors.groupingBy(ProjectRoleRequirements::getProjectId));

        // 4) 多因子打分并排序（可解释）
        LocalDateTime now = LocalDateTime.now();
        List<ScoredProject> scored = new ArrayList<>();
        for (Project project : candidates) {
            List<ProjectRoleRequirements> reqs =
                    requirementsByProject.getOrDefault(project.getProjectId(), Collections.emptyList());

            double score = calculateMatchScore(project, reqs, userSkillWeights, trackPreference, now);
            scored.add(new ScoredProject(project, score));
        }

        scored.sort(Comparator.comparingDouble(ScoredProject::score).reversed());

        // 5) 取 TopN
        int limit = Math.min(30, scored.size());
        List<Project> topProjects = scored.subList(0, limit).stream().map(ScoredProject::project).toList();

        return buildProjectListResponses(topProjects);
    }

    private List<Project> queryMatchedCandidates(boolean requireAuditApproved) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, ProjectStatusEnum.RECRUITING.getCode());
        if (requireAuditApproved) {
            wrapper.eq(Project::getAuditStatus, 1);
        }
        wrapper.gt(Project::getDeadlineRecruit, LocalDateTime.now());
        wrapper.orderByDesc(Project::getReleaseTime);

        // 先取一定量候选，避免全表扫描过大（可根据数据量调整）
        Page<Project> page = new Page<>(1, 200);
        Page<Project> res = projectMapper.selectPage(page, wrapper);
        List<Project> list = res.getRecords();
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 用户技能画像：综合 user_skill_relation(带熟练度) + User.techStack 文本解析
     */
    private Map<String, Double> buildUserSkillWeights(Integer userId, String techStackText) {
        Map<String, Double> weights = new HashMap<>();

        // 1) user_skill_relation -> tagName
        LambdaQueryWrapper<club.boyuan.official.teammatching.entity.UserSkillRelation> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.eq(club.boyuan.official.teammatching.entity.UserSkillRelation::getUserId, userId);
        List<club.boyuan.official.teammatching.entity.UserSkillRelation> relations =
                userSkillRelationMapper.selectList(wrapper);

        if (relations != null && !relations.isEmpty()) {
            List<Integer> tagIds = relations.stream()
                    .map(club.boyuan.official.teammatching.entity.UserSkillRelation::getTagId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!tagIds.isEmpty()) {
                Map<Integer, String> tagIdToName = skillTagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(
                                club.boyuan.official.teammatching.entity.SkillTag::getTagId,
                                club.boyuan.official.teammatching.entity.SkillTag::getTagName,
                                (a, b) -> a
                        ));

                for (club.boyuan.official.teammatching.entity.UserSkillRelation rel : relations) {
                    String name = tagIdToName.get(rel.getTagId());
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    String token = normalizeToken(name);
                    // 熟练度：1-了解 2-熟悉 3-精通 -> 权重 0.6 / 0.85 / 1.0
                    double w = switch (rel.getProficiency() == null ? 1 : rel.getProficiency()) {
                        case 3 -> 1.0;
                        case 2 -> 0.85;
                        default -> 0.6;
                    };
                    weights.merge(token, w, Math::max);
                }
            }
        }

        // 2) techStack 文本解析（逗号/空格/顿号等）
        for (String t : splitTokens(techStackText)) {
            String token = normalizeToken(t);
            if (token.isBlank()) {
                continue;
            }
            // techStack 作为弱信号
            weights.merge(token, 0.5, Math::max);
        }

        return weights;
    }

    /**
     * 用户兴趣赛道偏好：综合 收藏项目 + 投递项目 的 belongTrack，越新权重越高
     */
    private Map<String, Double> buildUserTrackPreference(Integer userId) {
        Map<String, Double> preference = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 收藏：favorite.targetType=1 项目
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId).eq(Favorite::getTargetType, 1);
        favWrapper.orderByDesc(Favorite::getCreatedTime);
        Page<Favorite> favPage = new Page<>(1, 200);
        List<Favorite> favorites = favoriteMapper.selectPage(favPage, favWrapper).getRecords();

        if (favorites != null && !favorites.isEmpty()) {
            List<Integer> ids = favorites.stream().map(Favorite::getTargetId).filter(Objects::nonNull).distinct().toList();
            Map<Integer, Project> projectMap = ids.isEmpty()
                    ? new HashMap<>()
                    : projectMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Project::getProjectId, p -> p));

            for (Favorite fav : favorites) {
                Project p = projectMap.get(fav.getTargetId());
                if (p == null || p.getBelongTrack() == null) continue;
                double recency = timeDecay(fav.getCreatedTime(), now, 30); // 30天半衰
                preference.merge(p.getBelongTrack(), 2.0 * recency, Double::sum); // 收藏信号更强
            }
        }

        // 投递：team_application
        LambdaQueryWrapper<TeamApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(TeamApplication::getApplicantUserId, userId);
        appWrapper.orderByDesc(TeamApplication::getApplyTime);
        Page<TeamApplication> appPage = new Page<>(1, 200);
        List<TeamApplication> applications = teamApplicationMapper.selectPage(appPage, appWrapper).getRecords();

        if (applications != null && !applications.isEmpty()) {
            List<Integer> ids = applications.stream().map(TeamApplication::getProjectId).filter(Objects::nonNull).distinct().toList();
            Map<Integer, Project> projectMap = ids.isEmpty()
                    ? new HashMap<>()
                    : projectMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Project::getProjectId, p -> p));

            for (TeamApplication app : applications) {
                Project p = projectMap.get(app.getProjectId());
                if (p == null || p.getBelongTrack() == null) continue;
                double recency = timeDecay(app.getApplyTime(), now, 45); // 45天半衰
                preference.merge(p.getBelongTrack(), 1.2 * recency, Double::sum);
            }
        }

        // 归一化到 0~1
        double max = preference.values().stream().mapToDouble(v -> v).max().orElse(0.0);
        if (max > 0) {
            for (Map.Entry<String, Double> e : new HashMap<>(preference).entrySet()) {
                preference.put(e.getKey(), e.getValue() / max);
            }
        }
        return preference;
    }

    private double calculateMatchScore(Project project,
                                       List<ProjectRoleRequirements> requirements,
                                       Map<String, Double> userSkillWeights,
                                       Map<String, Double> trackPreference,
                                       LocalDateTime now) {
        // A. 兴趣赛道匹配（0~1）
        double trackScore = 0.0;
        if (project.getBelongTrack() != null) {
            trackScore = trackPreference.getOrDefault(project.getBelongTrack(), 0.0);
        }

        // B. 技能/角色匹配（0~1）：把项目文本向量化后，与用户技能权重做加权命中率
        Map<String, Double> projectTokens = buildProjectTokenWeights(project, requirements);
        double skillScore = weightedOverlap(userSkillWeights, projectTokens);

        // C. 紧迫度（deadline 越近越高，0~1）
        double urgencyScore = 0.0;
        if (project.getDeadlineRecruit() != null) {
            long days = Duration.between(now, project.getDeadlineRecruit()).toDays();
            days = Math.max(0, days);
            urgencyScore = 1.0 / (1.0 + days / 7.0); // 7天为尺度
        }

        // D. 岗位缺口（越缺人越高，0~1）
        double vacancyScore = 0.0;
        if (requirements != null && !requirements.isEmpty()) {
            double sum = 0.0;
            int cnt = 0;
            for (ProjectRoleRequirements r : requirements) {
                if (r.getMemberQuota() == null || r.getMemberQuota() <= 0) continue;
                int cur = r.getCurrentMembers() == null ? 0 : r.getCurrentMembers();
                double v = 1.0 - Math.min(1.0, (double) cur / r.getMemberQuota());
                sum += v;
                cnt++;
            }
            vacancyScore = cnt == 0 ? 0.0 : sum / cnt;
        }

        // E. 热度（0~1）：log 归一化
        double popularityRaw =
                Math.log1p(nullToZero(project.getViewCount())) +
                        0.7 * Math.log1p(nullToZero(project.getFavoriteCount())) +
                        0.6 * Math.log1p(nullToZero(project.getApplyCount()));
        double popularityScore = 1.0 - 1.0 / (1.0 + popularityRaw); // squash 到 0~1

        // F. 新鲜度（越新越高，0~1）
        double freshnessScore = timeDecay(project.getReleaseTime(), now, 21); // 21天半衰

        // 综合：强调“匹配度”为主，热度/新鲜度为辅，紧迫度/缺口做微调
        return 0.38 * skillScore
                + 0.18 * trackScore
                + 0.14 * vacancyScore
                + 0.12 * urgencyScore
                + 0.10 * popularityScore
                + 0.08 * freshnessScore;
    }

    private Map<String, Double> buildProjectTokenWeights(Project project, List<ProjectRoleRequirements> requirements) {
        Map<String, Double> tokens = new HashMap<>();

        // 赛道/标签是强信号
        for (String t : splitTokens(project.getBelongTrack())) {
            tokens.merge(normalizeToken(t), 0.8, Double::max);
        }
        for (String t : splitTokens(project.getTags())) {
            tokens.merge(normalizeToken(t), 1.0, Double::max);
        }

        // 项目名称/简介/特点是中信号
        for (String t : splitTokens(project.getName())) {
            tokens.merge(normalizeToken(t), 0.6, Double::max);
        }
        for (String t : splitTokens(project.getProjectIntro())) {
            tokens.merge(normalizeToken(t), 0.35, Double::max);
        }
        for (String t : splitTokens(project.getProjectFeatures())) {
            tokens.merge(normalizeToken(t), 0.35, Double::max);
        }

        // 角色要求：role 强，recruitRequirements 次之
        if (requirements != null) {
            for (ProjectRoleRequirements r : requirements) {
                for (String t : splitTokens(r.getRole())) {
                    tokens.merge(normalizeToken(t), 0.9, Double::max);
                }
                for (String t : splitTokens(r.getRecruitRequirements())) {
                    tokens.merge(normalizeToken(t), 0.5, Double::max);
                }
            }
        }

        // 去掉空 token
        tokens.entrySet().removeIf(e -> e.getKey() == null || e.getKey().isBlank());
        return tokens;
    }

    /**
     * 加权重叠：sum(min(w_u, w_p)) / sum(w_u)
     */
    private double weightedOverlap(Map<String, Double> user, Map<String, Double> project) {
        if (user == null || user.isEmpty() || project == null || project.isEmpty()) {
            return 0.0;
        }
        double denom = user.values().stream().mapToDouble(v -> v).sum();
        if (denom <= 0) return 0.0;

        double num = 0.0;
        for (Map.Entry<String, Double> e : user.entrySet()) {
            Double pw = project.get(e.getKey());
            if (pw == null) continue;
            num += Math.min(e.getValue(), pw);
        }
        return Math.max(0.0, Math.min(1.0, num / denom));
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 指数时间衰减：halfLifeDays 半衰期；t 为空时返回 0
     */
    private static double timeDecay(LocalDateTime t, LocalDateTime now, int halfLifeDays) {
        if (t == null || now == null) return 0.0;
        long days = Math.max(0, Duration.between(t, now).toDays());
        double lambda = Math.log(2.0) / Math.max(1.0, halfLifeDays);
        return Math.exp(-lambda * days);
    }

    private static final Pattern TOKEN_SPLIT =
            Pattern.compile("[,，;；、\\s/|\\\\]+");

    private static List<String> splitTokens(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(TOKEN_SPLIT.split(text))
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }

    private static String normalizeToken(String raw) {
        if (raw == null) return "";
        String t = raw.trim().toLowerCase(Locale.ROOT);
        // 简单清洗：去掉常见标点
        t = t.replaceAll("[\\p{Punct}]+", "");
        return t;
    }

    private record ScoredProject(Project project, double score) { }

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