package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.user.AddSkillCertRequest;
import club.boyuan.official.teammatching.dto.request.user.UpdateProfileRequest;
import club.boyuan.official.teammatching.dto.response.user.AddSkillCertResponse;
import club.boyuan.official.teammatching.dto.response.user.SkillCertInfoResponse;
import club.boyuan.official.teammatching.dto.response.user.UserProfileResponse;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取当前用户资料
     * @param userId 用户 ID
     * @return 用户资料响应
     */
    UserProfileResponse getUserProfile(Integer userId);
    
    /**
     * 更新用户资料
     * @param userId 用户 ID
     * @param request 更新请求参数
     */
    void updateUserProfile(Integer userId, UpdateProfileRequest request);
    
    /**
     * 添加技能认证
     * @param addSkillCertRequest 添加技能认证请求参数
     * @param userId 用户 ID
     * @return 添加响应信息
     */
    AddSkillCertResponse addSkillCert(AddSkillCertRequest addSkillCertRequest, Integer userId);
    
    /**
     * 获取技能认证列表
     * @param userId 用户 ID
     * @return 技能认证列表
     */
    java.util.List<SkillCertInfoResponse> getSkillCertList(Integer userId);
}