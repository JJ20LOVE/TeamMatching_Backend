package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.admin.AuditListResponse;
import club.boyuan.official.teammatching.dto.response.admin.AuditVerifyResponse;
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
//@Validated
@RequiredArgsConstructor
//@NeedLogin  // TODO: 临时注释用于测试，生产环境需要取消注释
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
}