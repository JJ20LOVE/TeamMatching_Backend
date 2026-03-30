package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.request.admin.ContentAuditRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.admin.*;
import club.boyuan.official.teammatching.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员相关控制器
 * 所有接口都需要 JWT 认证（Authorization: Bearer <token>）
 */
@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
@NeedLogin
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取待审核列表
     *
     * @param request 审核请求参数
     * @return 待审核列表
     */
    @GetMapping("/auth/pending")
    public CommonResponse<AuditListResponse> getPendingAuthList(
            @ModelAttribute AuditRequest request) {
        AuditListResponse response = adminService.getPendingAuthList(request);
        return CommonResponse.ok(response);
    }

    /**
     * 审核认证申请
     *
     * @param authId 用户ID（认证ID）
     * @param request 审核请求
     * @return 审核结果
     */
    @PostMapping("/auth/{authId}/audit")
    public CommonResponse<AuditVerifyResponse> auditAuth(
            @PathVariable Integer authId,
            @RequestBody @Validated AuditVerifyRequest request) {
        AuditVerifyResponse response = adminService.auditAuth(authId, request);
        return CommonResponse.ok(response);
    }

    /**
     * 获取待审核内容
     *
     * @param request 内容审核请求（包含 type: project/post/comment）
     * @return 待审核内容列表
     */
    @GetMapping("/audit/contents")
    public CommonResponse<ContentAuditResponse> getAuditContents(
            @ModelAttribute ContentAuditRequest request) {
        ContentAuditResponse response = adminService.getAuditContents(request);
        return CommonResponse.ok(response);
    }

    /**
     * 审核认证内容
     * @param contentType
     * @return 审核结果
     */
    @PostMapping("audit/{contentType}/{contentId}")
    public CommonResponse<ContentVerifyResponse> auditContent(
            @PathVariable String contentType,
            @PathVariable Integer contentId,
            @RequestBody @Validated AuditVerifyRequest request
    ){
        ContentVerifyResponse response = adminService.verifyContent(contentType, contentId, request);
        return CommonResponse.ok(response);
    }

    @GetMapping("stats/dashboard")
    public CommonResponse<StatisticsResponse> getDashboardStats() {
        return CommonResponse.ok(adminService.getStatistics());
    }

}