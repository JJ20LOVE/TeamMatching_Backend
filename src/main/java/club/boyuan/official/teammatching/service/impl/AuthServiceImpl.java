package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.common.enums.AuthStatusEnum;
import club.boyuan.official.teammatching.common.utils.JwtUtils;
import club.boyuan.official.teammatching.common.utils.RedisUtils;
import club.boyuan.official.teammatching.common.utils.SecurityUtils;
import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.AuthService;
import club.boyuan.official.teammatching.service.EmailService;
import club.boyuan.official.teammatching.service.SmsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisUtils redisUtils;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Value("${app.environment:test}")
    private String environment;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("开始处理用户注册请求，账号: {}", registerRequest.getAccount());
        
        // 1. 校验验证码
        validateVerifyCode(registerRequest.getAccount(), registerRequest.getVerifyCode());
        
        // 2. 校验账号是否已存在
        validateAccountNotExist(registerRequest.getAccount());
        
        // 3. 创建用户
        User user = createUser(registerRequest);
        
        // 4. 保存用户到数据库
        int insertResult = userMapper.insert(user);
        if (insertResult <= 0) {
            throw new BusinessException("用户注册失败");
        }
        
        // 5. 清除验证码
        clearVerifyCode(registerRequest.getAccount());
        
        // 6. 生成JWT令牌
        String token = JwtUtils.generateToken(user.getUserId());
        
        // 7. 将令牌存储到Redis
        storeTokenToRedis(user.getUserId(), token);
        
        // 8. 构造响应
        RegisterResponse response = new RegisterResponse();
        response.setUserId(user.getUserId());
        response.setToken(token);
        response.setExpiresIn(JwtUtils.getExpirationTimeInSeconds());
        response.setAuthStatus(user.getAuthStatus());
        
        log.info("用户注册成功，用户ID: {}", user.getUserId());
        return response;
    }
    
    @Override
    public void sendVerifyCode(SendVerifyCodeRequest sendVerifyCodeRequest) {
        log.info("开始处理发送验证码请求，目标: {}, 类型: {}", 
                sendVerifyCodeRequest.getTarget(), sendVerifyCodeRequest.getType());
        
        String target = sendVerifyCodeRequest.getTarget();
        String type = sendVerifyCodeRequest.getType();
        
        // 1. 检查发送频率限制
        checkSendFrequency(target, type);
        
        // 2. 根据目标类型生成验证码
        String verifyCode = generateVerifyCode();
        
        // 3. 存储验证码到Redis
        storeVerifyCode(target, type, verifyCode);
        
        // 4. 发送验证码
        if (SecurityUtils.isEmail(target)) {
            emailService.sendHtmlVerifyCode(target, verifyCode, "TeamMatch 验证码");
        } else if (SecurityUtils.isPhone(target)) {
            smsService.sendVerifyCode(target, verifyCode);
        }
        
        log.info("验证码发送成功: {}, 类型: {}", target, type);
    }
    
    /**
     * 检查发送频率限制
     */
    private void checkSendFrequency(String account, String type) {
        String frequencyKey = String.format("verify_code_frequency:%s:%s", type, account);
        String lastSendTime = (String) redisUtils.get(frequencyKey);
        
        if (lastSendTime != null) {
            long lastTime = Long.parseLong(lastSendTime);
            long currentTime = System.currentTimeMillis();
            long interval = 60 * 1000; // 1分钟间隔
            
            if (currentTime - lastTime < interval) {
                long remainingTime = (interval - (currentTime - lastTime)) / 1000;
                throw new BusinessException(String.format("发送过于频繁，请%d秒后再试", remainingTime));
            }
        }
        
        // 记录本次发送时间
        redisUtils.set(frequencyKey, String.valueOf(System.currentTimeMillis()), 300); // 5分钟过期
    }
    
    /**
     * 生成6位随机数字验证码
     */
    private String generateVerifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
    
    /**
     * 存储验证码到Redis
     */
    private void storeVerifyCode(String account, String type, String verifyCode) {
        String key = String.format(RedisConstants.REGISTER_VERIFY_CODE_KEY, account);
        redisUtils.set(key, verifyCode, RedisConstants.VERIFY_CODE_EXPIRE_TIME * 60);
        
        // 记录验证码类型
        String typeKey = String.format("verify_code_type:%s", account);
        redisUtils.set(typeKey, type, RedisConstants.VERIFY_CODE_EXPIRE_TIME * 60);
    }
    
    /**
     * 校验验证码
     */
    private void validateVerifyCode(String account, String verifyCode) {
        String key = String.format(RedisConstants.REGISTER_VERIFY_CODE_KEY, account);
        String storedCode = (String) redisUtils.get(key);
        
        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(verifyCode)) {
            throw new BusinessException("验证码错误");
        }
    }
    
    /**
     * 校验账号是否已存在
     */
    private void validateAccountNotExist(String account) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (SecurityUtils.isEmail(account)) {
            queryWrapper.eq("email", account);
        } else if (SecurityUtils.isPhone(account)) {
            queryWrapper.eq("phone", account);
        } else {
            throw new BusinessException("账号格式不正确");
        }
        
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException("该账号已被注册");
        }
    }
    
    /**
     * 创建用户对象
     */
    private User createUser(RegisterRequest registerRequest) {
        User user = new User();
        String account = registerRequest.getAccount();
        
        // 设置账号信息
        if (SecurityUtils.isEmail(account)) {
            user.setEmail(account);
        } else if (SecurityUtils.isPhone(account)) {
            user.setPhone(account);
        }
        
        // 设置基本信息
        user.setPassword(SecurityUtils.encryptPassword(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname() != null ? 
                         registerRequest.getNickname() : "用户" + System.currentTimeMillis());
        user.setUsername(""); // 真实姓名暂为空
        user.setRole("student"); // 默认角色为学生
        user.setAuthStatus(AuthStatusEnum.PENDING.getCode()); // 默认待审核状态
        user.setStatus(false); // 默认未冻结
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        return user;
    }
    
    /**
     * 清除验证码
     */
    private void clearVerifyCode(String account) {
        String key = String.format(RedisConstants.REGISTER_VERIFY_CODE_KEY, account);
        redisUtils.del(key);
    }
    
    /**
     * 将令牌存储到Redis
     */
    private void storeTokenToRedis(Integer userId, String token) {
        String key = String.format(RedisConstants.USER_JWT_TOKEN_KEY, userId);
        redisUtils.set(key, token, RedisConstants.JWT_TOKEN_EXPIRE_TIME * 60 * 60);
    }
}