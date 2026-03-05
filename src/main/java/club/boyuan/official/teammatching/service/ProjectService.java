package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
import club.boyuan.official.teammatching.entity.Project;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {
    
    /**
     * 创建项目
     * @param userId 用户 ID
     * @param request 创建请求
     * @return 项目 ID
     */
    Integer createProject(Integer userId, CreateProjectRequest request);
    
    /**
     * 更新项目
     * @param projectId 项目 ID
     * @param userId 用户 ID
     * @param request 更新请求
     */
    void updateProject(Integer projectId, Integer userId, UpdateProjectRequest request);
    
    /**
     * 获取项目详情
     * @param projectId 项目 ID
     * @param currentUserId 当前登录用户 ID（可为 null）
     * @return 项目详情
     */
    ProjectDetailResponse getProjectDetail(Integer projectId, Integer currentUserId);
    
    /**
     * 获取我发布的项目列表
     * @param userId 用户 ID
     * @return 项目列表
     */
    List<Project> getMyPublishedProjects(Integer userId);
}