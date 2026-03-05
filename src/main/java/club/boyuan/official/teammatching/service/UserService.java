package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.user.UpdateProfileRequest;
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
}