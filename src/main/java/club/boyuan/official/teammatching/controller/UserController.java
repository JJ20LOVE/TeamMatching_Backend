package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.user.AddSkillCertRequest;
import club.boyuan.official.teammatching.dto.request.user.UpdateProfileRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.user.AddSkillCertResponse;
import club.boyuan.official.teammatching.dto.response.user.SkillCertInfoResponse;
import club.boyuan.official.teammatching.dto.response.user.UserProfileResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户接口")
@Validated
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取个人资料接口
     * @return 用户资料响应信息
     */
    @GetMapping("/profile")
    @ApiOperation(value = "获取个人资料", notes = "获取当前登录用户的详细资料")
    @NeedLogin
    public ResponseEntity<CommonResponse<UserProfileResponse>> getUserProfile() {
        log.info("收到获取个人资料请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            UserProfileResponse response = userService.getUserProfile(userId);
            log.info("获取个人资料成功，userId={}", userId);
            
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("获取个人资料失败，userId={}, error={}", UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新个人资料接口
     * @param request 更新请求参数
     * @return 响应信息
     */
    @PutMapping("/profile")
    @ApiOperation(value = "更新个人资料", notes = "更新当前登录用户的个人资料")
    @NeedLogin
    public ResponseEntity<CommonResponse<Void>> updateUserProfile(
            @ApiParam(value = "更新个人资料请求参数", required = true)
            @Valid @RequestBody UpdateProfileRequest request) {
        
        log.info("收到更新个人资料请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            userService.updateUserProfile(userId, request);
            log.info("更新个人资料成功，userId={}", userId);
            
            return ResponseEntity.ok(CommonResponse.ok("更新成功", null));
        } catch (Exception e) {
            log.error("更新个人资料失败，userId={}, error={}", UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }

    
    /**
     * 添加技能认证接口
     * @param addSkillCertRequest 添加技能认证请求参数
     * @return 响应信息
     */
    @PostMapping("/skill-cert")
    @ApiOperation(value = "添加技能认证", notes = "添加技能认证证书（对应功能 F-05）")
    @NeedLogin
    public ResponseEntity<CommonResponse<AddSkillCertResponse>> addSkillCert(
            @ApiParam(value = "添加技能认证请求参数", required = true)
            @Valid @RequestBody AddSkillCertRequest addSkillCertRequest) {
        
        log.info("收到添加技能认证请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            AddSkillCertResponse response = userService.addSkillCert(addSkillCertRequest, userId);
            log.info("技能认证添加成功，userId={}, certId={}", userId, response.getCertId());
            
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("技能认证添加失败，userId={}, error={}", UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取技能认证列表接口
     * @return 技能认证列表
     */
    @GetMapping("/skill-cert")
    @ApiOperation(value = "获取技能认证列表", notes = "获取当前用户的技能认证列表（对应功能 F-05）")
    @NeedLogin
    public ResponseEntity<CommonResponse<List<SkillCertInfoResponse>>> getSkillCertList() {
        
        log.info("收到获取技能认证列表请求");
        
        try {
            // 从上下文中获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            List<SkillCertInfoResponse> responseList = userService.getSkillCertList(userId);
            log.info("技能认证列表获取成功，userId={}, count={}", userId, responseList.size());
            
            return ResponseEntity.ok(CommonResponse.ok(responseList));
        } catch (Exception e) {
            log.error("技能认证列表获取失败，userId={}, error={}", UserContextUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }
}