package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.team.CreateTaskRequest;
import club.boyuan.official.teammatching.dto.request.team.CreateTeamPostRequest;
import club.boyuan.official.teammatching.dto.request.team.UpdateTaskStatusRequest;
import club.boyuan.official.teammatching.dto.response.team.MyTeamListResponse;
import club.boyuan.official.teammatching.dto.response.team.TaskResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamDetailResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamPostListResponse;

/**
 * 团队服务接口
 */
public interface TeamService {
    /**
     * 获取我的团队列表
     */
    MyTeamListResponse getMyTeams(Integer userId);

    /**
     * 获取团队详情
     */
    TeamDetailResponse getTeamDetail(Integer projectId, Integer userId);

    /**
     * 发布团队帖子
     */
    Integer createTeamPost(Integer projectId, Integer userId, CreateTeamPostRequest request);

    /**
     * 获取团队帖子列表
     */
    TeamPostListResponse getTeamPosts(Integer projectId, Integer userId, Integer page, Integer size);

    /**
     * 创建任务
     */
    TaskResponse createTask(Integer projectId, Integer userId, CreateTaskRequest request);

    /**
     * 更新任务状态
     */
    TaskResponse updateTaskStatus(Integer taskId, Integer userId, UpdateTaskStatusRequest request);

    /**
     * 移除成员
     */
    void removeMember(Integer projectId, Integer captainId, Integer targetUserId);

    /**
     * 成员退出团队
     */
    void quitTeam(Integer projectId, Integer userId);
}
