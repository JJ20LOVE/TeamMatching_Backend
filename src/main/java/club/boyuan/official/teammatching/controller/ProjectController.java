package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.project.CreateProjectRequest;
import club.boyuan.official.teammatching.dto.request.project.UpdateProjectRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.project.ProjectDetailResponse;
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
    @GetMapping("/list/my-published")
    @ApiOperation(value = "获取我发布的项目列表", notes = "获取当前用户发布的所有项目")
    @NeedLogin
    public ResponseEntity<CommonResponse<List<Project>>> getMyPublishedProjects() {
        
        log.info("收到获取我发布的项目列表请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            List<Project> projects = projectService.getMyPublishedProjects(userId);
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
}
