package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.project.ApplyProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.ProjectQueryRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.project.ApplyProjectResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectCardResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectListResponse;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Api(tags = "项目管理")
public class ProjectController {
    
    private final ProjectService projectService;
    
    /**
     * 创建项目
     */
    @PostMapping
    @ApiOperation(value = "创建项目", notes = "创建新项目招募")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, Object>>> createProject(
            @ApiParam(value = "创建项目请求参数", required = true)
            @Valid @RequestBody CreateProjectRequest request) {
        
        log.info("收到创建项目请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            Integer projectId = projectService.createProject(userId, request);
            log.info("项目创建成功：userId={}, projectId={}", userId, projectId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("projectId", projectId);
            data.put("message", "创建成功，待审核");
            
            return ResponseEntity.ok(CommonResponse.ok("创建成功", data));
        } catch (Exception e) {
            log.error("创建项目失败：userId={}, error={}", UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    } 
    
    /**
     * 更新项目
     */
    @PutMapping("/{projectId}")
    @ApiOperation(value = "更新项目", notes = "更新项目信息")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, String>>> updateProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "更新项目请求参数", required = true)
            @Valid @RequestBody UpdateProjectRequest request) {
        
        log.info("收到更新项目请求，projectId: {}", projectId);
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            projectService.updateProject(projectId, userId, request);
            log.info("项目更新成功：userId={}, projectId={}", userId, projectId);
            
            Map<String, String> data = new HashMap<>();
            data.put("message", "更新成功");
            
            return ResponseEntity.ok(CommonResponse.ok("更新成功", data));
        } catch (Exception e) {
            log.error("更新项目失败：userId={}, projectId={}, error={}", 
                    UserContextUtil.getCurrentUserId(), projectId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取我发布的项目列表
     */
    @GetMapping("/my-published")
    @ApiOperation(value = "获取我发布的项目列表", notes = "获取当前用户发布的所有项目")
    @NeedLogin
    public ResponseEntity<CommonResponse<List<ProjectCardResponse>>> getMyPublishedProjects(
            @ApiParam(value = "按项目状态筛选")
            @RequestParam(value = "status", required = false) Integer status,
            @ApiParam(value = "按审核状态筛选")
            @RequestParam(value = "auditStatus", required = false) Integer auditStatus,
            @ApiParam(value = "页码")
            @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "每页数量")
            @RequestParam(value = "size", required = false) Integer size) {
        
        log.info("收到获取我发布的项目列表请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            List<ProjectCardResponse> projects = projectService.getMyPublishedProjects(
                    userId, status, auditStatus, page, size);
            log.info("获取我发布的项目列表成功：userId={}, count={}", userId, 
                    projects != null ? projects.size() : 0);
            
            return ResponseEntity.ok(CommonResponse.ok(projects));
        } catch (Exception e) {
            log.error("获取我发布的项目列表失败：userId={}, error={}", 
                    UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取项目详情
     */
    @GetMapping("/{projectId}")
    @ApiOperation(value = "获取项目详情", notes = "获取项目的详细信息")
    public ResponseEntity<CommonResponse<ProjectDetailResponse>> getProjectDetail(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId) {
        
        log.info("收到获取项目详情请求，projectId: {}", projectId);
        
        try {
            // 从上下文中获取当前登录用户 ID（可选，未登录时为 null）
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            
            ProjectDetailResponse response = projectService.getProjectDetail(projectId, currentUserId);
            log.info("项目详情获取成功：projectId={}", projectId);
            
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("获取项目详情失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 立即沟通（投递申请）
     */
    @PostMapping("/{projectId}/apply")
    @ApiOperation(value = "立即沟通（投递）", notes = "学生向项目投递申请")
    @NeedLogin
    public ResponseEntity<CommonResponse<ApplyProjectResponse>> applyProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "投递申请请求参数", required = true)
            @Valid @RequestBody ApplyProjectRequest request) {

        log.info("收到项目投递申请，请求 projectId: {}", projectId);

        try {
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            ApplyProjectResponse data = projectService.applyProject(projectId, userId, request);

            return ResponseEntity.ok(CommonResponse.ok("投递成功", data));
        } catch (Exception e) {
            log.error("项目投递申请失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PATCH 更新项目状态
     */
    @PatchMapping("/{projectId}/status")
    @ApiOperation(value = "更新项目状态", notes = "下架、关闭等状态更新")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, String>>> updateProjectStatus(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId,
            @ApiParam(value = "要更新的状态：0-草拟 1-实施 2-招募中 3-完成 4-终止", required = true)
            @RequestBody Map<String, Integer> body) {

        log.info("收到更新项目状态请求，projectId: {}", projectId);

        try {
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            Integer status = body.get("status");
            if (status == null) {
                throw new BusinessException("状态不能为空");
            }

            projectService.updateProjectStatus(projectId, userId, status);

            Map<String, String> data = new HashMap<>();
            data.put("message", "状态更新成功");

            return ResponseEntity.ok(CommonResponse.ok("状态更新成功", data));
        } catch (Exception e) {
            log.error("更新项目状态失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET 获取项目统计数据
     */
    @GetMapping("/{projectId}/stats")
    @ApiOperation(value = "获取项目统计数据", notes = "获取项目的浏览、收藏、申请等统计数据")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getProjectStats(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId) {

        log.info("收到获取项目统计数据请求，projectId: {}", projectId);

        try {
            Map<String, Object> data = projectService.getProjectStats(projectId);
            return ResponseEntity.ok(CommonResponse.ok(data));
        } catch (Exception e) {
            log.error("获取项目统计数据失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET 首页项目卡片流（项目广场）
     */
    @GetMapping("/list")
    @ApiOperation(value = "首页项目卡片流", notes = "项目广场，获取项目列表")
    public ResponseEntity<CommonResponse<List<ProjectListResponse>>> getProjectList(
            @ApiParam(value = "所属赛道筛选")
            @RequestParam(value = "track", required = false) String track,
            @ApiParam(value = "所需角色筛选")
            @RequestParam(value = "role", required = false) String role,
            @ApiParam(value = "排序：latest(最新) hot(最热) deadline(截止最近)")
            @RequestParam(value = "sort", required = false) String sort,
            @ApiParam(value = "搜索关键词")
            @RequestParam(value = "keyword", required = false) String keyword,
            @ApiParam(value = "页码")
            @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "每页数量")
            @RequestParam(value = "size", required = false) Integer size) {

        log.info("收到获取项目列表请求");

        try {
            ProjectQueryRequest request = new ProjectQueryRequest();
            request.setTrack(track);
            request.setRole(role);
            request.setSort(sort);
            request.setKeyword(keyword);
            request.setPage(page);
            request.setSize(size);

            List<ProjectListResponse> list = projectService.getProjectList(request);
            return ResponseEntity.ok(CommonResponse.ok(list));
        } catch (Exception e) {
            log.error("获取项目列表失败，error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET 获取相似项目推荐
     */
    @GetMapping("/{projectId}/similar")
    @ApiOperation(value = "获取相似项目", notes = "获取相似项目推荐")
    public ResponseEntity<CommonResponse<List<ProjectListResponse>>> getSimilarProjects(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId) {

        log.info("收到获取相似项目请求，projectId: {}", projectId);

        try {
            List<ProjectListResponse> list = projectService.getSimilarProjects(projectId);
            return ResponseEntity.ok(CommonResponse.ok(list));
        } catch (Exception e) {
            log.error("获取相似项目失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET 智能匹配项目
     */
    @GetMapping("/match")
    @ApiOperation(value = "智能匹配项目", notes = "根据用户画像智能匹配推荐项目")
    @NeedLogin
    public ResponseEntity<CommonResponse<List<ProjectListResponse>>> getMatchedProjects() {

        log.info("收到智能匹配项目请求");

        try {
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            List<ProjectListResponse> list = projectService.getMatchedProjects(userId);
            return ResponseEntity.ok(CommonResponse.ok(list));
        } catch (Exception e) {
            log.error("获取智能匹配项目失败，error={}", e.getMessage(), e);
            throw e;
        }
    }
}
