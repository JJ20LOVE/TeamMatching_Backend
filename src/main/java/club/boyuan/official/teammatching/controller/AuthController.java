package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import club.boyuan.official.teammatching.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * 认证相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证接口")
@Validated
@RequiredArgsConstructor
public class AuthController {
    

    private final AuthService authService;
    
    /**
     * 用户注册接口
     * @param registerRequest 注册请求参数
     * @return 注册响应信息
     */
    @PostMapping("/register")
    @ApiOperation(value = "邮箱/手机号注册", notes = "用户通过邮箱或手机号进行注册")
    public ResponseEntity<RegisterResponse> register(
            @ApiParam(value = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        log.info("收到注册请求: account={}", registerRequest.getAccount());
        
        try {
            RegisterResponse response = authService.register(registerRequest);
            log.info("注册成功: userId={}", response.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注册失败: account={}, error={}", registerRequest.getAccount(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 发送验证码接口
     * @param sendVerifyCodeRequest 发送验证码请求参数
     */
    @PostMapping("/send-code")
    @ApiOperation(value = "发送验证码", notes = "向邮箱或手机号发送验证码")
    public ResponseEntity<Void> sendVerifyCode(
            @ApiParam(value = "发送验证码请求参数", required = true)
            @Valid @RequestBody SendVerifyCodeRequest sendVerifyCodeRequest) {
        
        log.info("收到发送验证码请求: target={}, type={}", 
                sendVerifyCodeRequest.getTarget(), sendVerifyCodeRequest.getType());
        
        try {
            authService.sendVerifyCode(sendVerifyCodeRequest);
            log.info("验证码发送成功: target={}", sendVerifyCodeRequest.getTarget());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("发送验证码失败: target={}, error={}", 
                    sendVerifyCodeRequest.getTarget(), e.getMessage(), e);
            throw e;
        }
    }
    
    // 其他认证相关API
}