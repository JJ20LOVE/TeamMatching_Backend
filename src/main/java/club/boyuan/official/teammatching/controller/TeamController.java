package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedAuth;
import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.team.CreateTaskRequest;
import club.boyuan.official.teammatching.dto.request.team.CreateTeamPostRequest;
import club.boyuan.official.teammatching.dto.request.team.UpdateTaskStatusRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.team.MyTeamListResponse;
import club.boyuan.official.teammatching.dto.response.team.TaskResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamDetailResponse;
import club.boyuan.official.teammatching.dto.response.team.TeamPostListResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 团队相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/team")
@Api(tags = "团队空间")
@Validated
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    /**
     * 获取我的团队列表
     */
    @GetMapping("/my")
    @ApiOperation(value = "获取我的团队列表")
    @NeedLogin
    public ResponseEntity<CommonResponse<MyTeamListResponse>> getMyTeams() {
        Integer userId = getCurrentUserId();
        MyTeamListResponse response = teamService.getMyTeams(userId);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    /**
     * 获取团队详情
     */
    @GetMapping("/{projectId}")
    @ApiOperation(value = "获取团队详情")
    @NeedLogin
    public ResponseEntity<CommonResponse<TeamDetailResponse>> getTeamDetail(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId) {
        Integer userId = getCurrentUserId();
        TeamDetailResponse response = teamService.getTeamDetail(projectId, userId);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    /**
     * 发布团队内部帖子
     */
    @PostMapping("/{projectId}/post")
    @ApiOperation(value = "发布团队内部帖子")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, Object>>> createTeamPost(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "帖子请求体", required = true)
            @Valid @RequestBody CreateTeamPostRequest request) {
        Integer userId = getCurrentUserId();
        Integer postId = teamService.createTeamPost(projectId, userId, request);

        Map<String, Object> data = new HashMap<>();
        data.put("postId", postId);
        data.put("message", "发布成功");
        return ResponseEntity.ok(CommonResponse.ok("发布成功", data));
    }

    /**
     * 获取团队讨论列表
     */
    @GetMapping("/{projectId}/posts")
    @ApiOperation(value = "获取团队讨论列表")
    @NeedLogin
    @NeedAuth
    public ResponseEntity<CommonResponse<TeamPostListResponse>> getTeamPosts(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "页码")
            @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "每页数量")
            @RequestParam(value = "size", required = false) Integer size) {
        Integer userId = getCurrentUserId();
        TeamPostListResponse response = teamService.getTeamPosts(projectId, userId, page, size);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    /**
     * 创建任务
     */
    @PostMapping("/{projectId}/task")
    @ApiOperation(value = "创建任务")
    @NeedLogin
    public ResponseEntity<CommonResponse<TaskResponse>> createTask(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "创建任务请求体", required = true)
            @Valid @RequestBody CreateTaskRequest request) {
        Integer userId = getCurrentUserId();
        TaskResponse response = teamService.createTask(projectId, userId, request);
        return ResponseEntity.ok(CommonResponse.ok("创建成功", response));
    }

    /**
     * 更新任务状态
     */
    @PatchMapping("/task/{taskId}")
    @ApiOperation(value = "更新任务状态")
    @NeedLogin
    public ResponseEntity<CommonResponse<TaskResponse>> updateTaskStatus(
            @ApiParam(value = "任务ID", required = true)
            @PathVariable Integer taskId,
            @ApiParam(value = "状态更新请求体", required = true)
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Integer userId = getCurrentUserId();
        TaskResponse response = teamService.updateTaskStatus(taskId, userId, request);
        return ResponseEntity.ok(CommonResponse.ok("更新成功", response));
    }

    /**
     * 移除成员
     */
    @DeleteMapping("/{projectId}/member/{userId}")
    @ApiOperation(value = "移除成员")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, String>>> removeMember(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "待移除成员ID", required = true)
            @PathVariable("userId") Integer targetUserId) {
        Integer currentUserId = getCurrentUserId();
        teamService.removeMember(projectId, currentUserId, targetUserId);

        Map<String, String> data = new HashMap<>();
        data.put("message", "成员已移除");
        return ResponseEntity.ok(CommonResponse.ok("操作成功", data));
    }

    /**
     * 成员退出
     */
    @PostMapping("/{projectId}/quit")
    @ApiOperation(value = "成员退出团队")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, String>>> quitTeam(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable Integer projectId) {
        Integer userId = getCurrentUserId();
        teamService.quitTeam(projectId, userId);

        Map<String, String> data = new HashMap<>();
        data.put("message", "已退出团队");
        return ResponseEntity.ok(CommonResponse.ok("操作成功", data));
    }

    private Integer getCurrentUserId() {
        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            log.warn("请求团队接口时用户未登录");
            throw new BusinessException("用户未登录");
        }
        return userId;
    }
}
