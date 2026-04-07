package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.request.admin.ContentAuditRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.common.annotation.NeedAdmin;
import club.boyuan.official.teammatching.dto.response.admin.AuditListResponse;
import club.boyuan.official.teammatching.dto.response.admin.AuditVerifyResponse;
import club.boyuan.official.teammatching.dto.response.admin.ContentAuditResponse;
import club.boyuan.official.teammatching.dto.response.admin.ContentVerifyResponse;
import club.boyuan.official.teammatching.dto.response.admin.StatisticsResponse;
import club.boyuan.official.teammatching.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员相关控制器
 * 需要 JWT，且当前用户 {@code role=admin}
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
@NeedAdmin
@Api(tags = "管理员接口")
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取待审核列表
     *
     * @param request 审核请求参数
     * @return 待审核列表
     */
    @GetMapping("/auth/pending")
    @ApiOperation(value = "获取待审核列表", notes = "获取待审核认证申请列表")
    public ResponseEntity<CommonResponse<AuditListResponse>> getPendingAuthList(
            @ApiParam(value = "审核分页参数", required = false)
            @ModelAttribute AuditRequest request) {
        log.info("收到获取待审核列表请求：page={}, size={}", request.getPage(), request.getSize());
        try {
            AuditListResponse response = adminService.getPendingAuthList(request);
            log.info("获取待审核列表成功：total={}", response.getTotal());
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("获取待审核列表失败：error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 审核认证申请
     *
     * @param authId 用户ID（认证ID）
     * @param request 审核请求
     * @return 审核结果
     */
    @PostMapping("/auth/{authId}/audit")
    @ApiOperation(value = "审核认证申请", notes = "审核用户身份认证申请（通过/驳回）")
    public ResponseEntity<CommonResponse<AuditVerifyResponse>> auditAuth(
            @ApiParam(value = "认证ID", required = true)
            @PathVariable Integer authId,
            @ApiParam(value = "审核请求参数", required = true)
            @Valid @RequestBody AuditVerifyRequest request) {
        log.info("收到审核认证申请请求：authId={}, result={}", authId, request.getResult());
        try {
            AuditVerifyResponse response = adminService.auditAuth(authId, request);
            log.info("审核认证申请成功：authId={}, authStatus={}", authId, response.getAuthStatus());
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("审核认证申请失败：authId={}, error={}", authId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取待审核内容
     *
     * @param request 内容审核请求（包含 type: project/post/comment）
     * @return 待审核内容列表
     */
    @GetMapping("/audit/contents")
    @ApiOperation(value = "获取待审核内容", notes = "获取待审核内容列表（project/post/comment）")
    public ResponseEntity<CommonResponse<ContentAuditResponse>> getAuditContents(
            @ApiParam(value = "内容类型筛选参数", required = false)
            @ModelAttribute ContentAuditRequest request) {
        log.info("收到获取待审核内容请求：type={}", request.getType());
        try {
            ContentAuditResponse response = adminService.getAuditContents(request);
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("获取待审核内容失败：type={}, error={}", request.getType(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 审核认证内容
     * @param contentType
     * @return 审核结果
     */
    @PostMapping("/audit/{contentType}/{contentId}")
    @ApiOperation(value = "审核内容", notes = "审核用户发布的内容（通过/驳回）")
    public ResponseEntity<CommonResponse<ContentVerifyResponse>> auditContent(
            @ApiParam(value = "内容类型", required = true)
            @PathVariable String contentType,
            @ApiParam(value = "内容ID", required = true)
            @PathVariable Integer contentId,
            @ApiParam(value = "审核请求参数", required = true)
            @Valid @RequestBody AuditVerifyRequest request) {
        log.info("收到审核内容请求：contentType={}, contentId={}, result={}", contentType, contentId, request.getResult());
        try {
            ContentVerifyResponse response = adminService.verifyContent(contentType, contentId, request);
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("审核内容失败：contentType={}, contentId={}, error={}", contentType, contentId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/stats/dashboard")
    @ApiOperation(value = "管理员看板统计", notes = "获取后台统计信息")
    public ResponseEntity<CommonResponse<StatisticsResponse>> getDashboardStats() {
        log.info("收到管理员看板统计请求");
        try {
            StatisticsResponse response = adminService.getStatistics();
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("管理员看板统计失败：error={}", e.getMessage(), e);
            throw e;
        }
    }

}