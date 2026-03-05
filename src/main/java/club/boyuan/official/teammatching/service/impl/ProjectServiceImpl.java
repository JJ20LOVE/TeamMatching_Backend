package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.ProjectRoleRequirements;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.ProjectRoleRequirementMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserMapper userMapper;
    
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
    public List<Project> getMyPublishedProjects(Integer userId) {
        log.info("获取我发布的项目列表，userId: {}", userId);
        
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getPublisherUserId, userId)
                   .orderByDesc(Project::getReleaseTime);
        
        return projectMapper.selectList(queryWrapper);
    }
}