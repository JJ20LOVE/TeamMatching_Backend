package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.response.admin.AuditListResponse;
import club.boyuan.official.teammatching.dto.response.admin.AuditVerifyResponse;

/**
 * 管理服务接口
 */
public interface AdminService {

    /**
     * 获取待审核列表
     *
     * @param request 审核请求参数
     * @return 待审核列表响应
     */
    AuditListResponse getPendingAuthList(AuditRequest request);

    /**
     * 审核认证申请
     *
     * @param authId 用户ID（认证ID）
     * @param request 审核请求
     * @return 审核结果响应
     */
    AuditVerifyResponse auditAuth(Integer authId, AuditVerifyRequest request);
}