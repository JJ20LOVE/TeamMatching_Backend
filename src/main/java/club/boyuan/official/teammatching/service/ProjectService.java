package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.project.ApplyProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.ProjectQueryRequest;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectCardResponse;
import club.boyuan.official.teammatching.dto.response.project.ApplyProjectResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectListResponse;
import club.boyuan.official.teammatching.entity.Project;

import java.util.List;
import java.util.Map;

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
     * @param userId      用户 ID
     * @param status      按项目状态筛选（可选）
     * @param auditStatus 按审核状态筛选（可选）
     * @param page        页码（可选）
     * @param size        每页数量（可选）
     * @return 项目卡片列表
     */
    List<ProjectCardResponse> getMyPublishedProjects(Integer userId,
                                                     Integer status,
                                                     Integer auditStatus,
                                                     Integer page,
                                                     Integer size);

    /**
     * 学生向项目投递申请（立即沟通）
     * @param projectId 项目 ID
     * @param userId    当前登录用户 ID
     * @param request   申请请求
     * @return 申请结果（包含申请 ID、会话 ID 等）
     */
    ApplyProjectResponse applyProject(Integer projectId, Integer userId, ApplyProjectRequest request);

    /**
     * 更新项目状态
     * @param projectId 项目 ID
     * @param userId    当前登录用户 ID
     * @param status    要更新的状态：0-草拟 1-实施 2-招募中 3-完成 4-终止
     */
    void updateProjectStatus(Integer projectId, Integer userId, Integer status);

    /**
     * 获取项目统计数据
     * @param projectId 项目 ID
     * @return 包含浏览、收藏、申请等统计数据的 Map
     */
    Map<String, Object> getProjectStats(Integer projectId);

    /**
     * 获取项目列表（项目广场、首页卡片流）
     * @param request 查询条件
     * @return 项目列表
     */
    List<ProjectListResponse> getProjectList(ProjectQueryRequest request);

    /**
     * 收藏/取消收藏项目（切换）
     * @param projectId 项目 ID
     * @param userId    当前登录用户 ID
     * @return 切换后的收藏状态
     */
    boolean toggleFavoriteProject(Integer projectId, Integer userId);

    /**
     * 关注/取消关注发布者（切换）
     * @param targetUserId 被关注者 ID
     * @param userId       当前登录用户 ID
     * @return 切换后的关注状态
     */
    boolean toggleFollowUser(Integer targetUserId, Integer userId);

    /**
     * 获取相似项目推荐
     * @param projectId 项目 ID
     * @return 相似项目列表
     */
    List<ProjectListResponse> getSimilarProjects(Integer projectId);

    /**
     * 智能匹配项目（基于用户画像的推荐）
     * @param userId 当前登录用户 ID
     * @return 匹配项目列表
     */
    List<ProjectListResponse> getMatchedProjects(Integer userId);
}