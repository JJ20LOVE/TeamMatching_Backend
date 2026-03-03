package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.constants.RedisConstants;
import club.boyuan.official.teammatching.common.enums.AuthStatusEnum;
import club.boyuan.official.teammatching.common.utils.JwtUtils;
import club.boyuan.official.teammatching.common.utils.RedisUtils;
import club.boyuan.official.teammatching.common.utils.SecurityUtils;
import club.boyuan.official.teammatching.dto.request.auth.ChangePasswordRequest;
import club.boyuan.official.teammatching.dto.request.auth.ForgotPasswordRequest;
import club.boyuan.official.teammatching.dto.request.auth.LoginRequest;
import club.boyuan.official.teammatching.dto.request.auth.RegisterRequest;
import club.boyuan.official.teammatching.dto.request.auth.SendVerifyCodeRequest;
import club.boyuan.official.teammatching.dto.request.auth.WxLoginRequest;
import club.boyuan.official.teammatching.dto.response.auth.RegisterResponse;
import club.boyuan.official.teammatching.dto.response.auth.WxLoginResponse;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.AuthService;
import club.boyuan.official.teammatching.service.EmailService;
import club.boyuan.official.teammatching.service.SmsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginResponse wxLogin(WxLoginRequest wxLoginRequest) {
        log.info("开始处理微信登录请求，code: {}", wxLoginRequest.getCode());
        
        // 1. 调用微信接口获取openid和session_key
        WxSessionResult sessionResult = getSessionKey(wxLoginRequest.getCode());
        
        // 2. 解密用户信息（如果提供了encryptedData和iv）
        WxUserInfo userInfo = null;
        if (wxLoginRequest.getEncryptedData() != null && wxLoginRequest.getIv() != null) {
            userInfo = decryptUserInfo(
                wxLoginRequest.getEncryptedData(), 
                wxLoginRequest.getIv(), 
                sessionResult.getSessionKey()
            );
        }
        
        // 3. 查找或创建用户
        User user = findOrCreateWxUser(sessionResult.getOpenid(), userInfo);
        
        // 4. 更新用户最后登录时间
        updateUserLoginInfo(user.getUserId());
        
        // 5. 生成JWT令牌
        String token = JwtUtils.generateToken(user.getUserId());
        
        // 6. 将令牌存储到Redis
        storeTokenToRedis(user.getUserId(), token);
        
        // 7. 构造响应
        WxLoginResponse response = new WxLoginResponse();
        response.setUserId(user.getUserId());
        response.setToken(token);
        response.setExpiresIn(JwtUtils.getExpirationTimeInSeconds());
        response.setAuthStatus(user.getAuthStatus());
        response.setIsNewUser(user.getCreatedTime().equals(user.getUpdateTime()));
        
        log.info("微信登录成功，用户ID: {}, 是否新用户: {}", user.getUserId(), response.getIsNewUser());
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse login(LoginRequest loginRequest) {
        log.info("开始处理用户登录请求，账号: {}", loginRequest.getAccount());
        
        // 1. 根据账号查找用户
        User user = findUserByAccount(loginRequest.getAccount());
        
        // 2. 校验密码
        validatePassword(loginRequest.getPassword(), user.getPassword());
        
        // 3. 检查用户状态
        validateUserStatus(user);
        
        // 4. 更新用户登录信息
        updateUserLoginInfo(user.getUserId());
        
        // 5. 生成JWT令牌
        String token = JwtUtils.generateToken(user.getUserId());
        
        // 6. 将令牌存储到Redis
        storeTokenToRedis(user.getUserId(), token);
        
        // 7. 构造响应
        RegisterResponse response = new RegisterResponse();
        response.setUserId(user.getUserId());
        response.setToken(token);
        response.setExpiresIn(JwtUtils.getExpirationTimeInSeconds());
        response.setAuthStatus(user.getAuthStatus());
        
        log.info("用户登录成功，用户ID: {}", user.getUserId());
        return response;
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
    
    /**
     * 根据账号查找用户
     */
    private User findUserByAccount(String account) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (SecurityUtils.isEmail(account)) {
            queryWrapper.eq("email", account);
        } else if (SecurityUtils.isPhone(account)) {
            queryWrapper.eq("phone", account);
        } else {
            // 学号查询
            queryWrapper.eq("student_id", account);
        }
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        
        return user;
    }
    
    /**
     * 校验密码
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!SecurityUtils.verifyPassword(rawPassword, encodedPassword)) {
            throw new BusinessException("密码错误");
        }
    }
    
    /**
     * 检查用户状态
     */
    private void validateUserStatus(User user) {
        if (user.getStatus() == null || user.getStatus()) {
            throw new BusinessException("账号已被冻结");
        }
    }
    
    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() != null ? user.getLoginCount() + 1 : 1);
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(user);
    }
    
    /**
     * 调用微信接口获取session_key和openid
     */
    private WxSessionResult getSessionKey(String code) {
        // TODO: 这里需要配置微信小程序的appid和secret
        // 暂时返回模拟数据用于开发测试
        WxSessionResult result = new WxSessionResult();
        result.setOpenid("mock_openid_" + code);
        result.setSessionKey("mock_session_key_" + code);
        return result;
        
        /*
        // 实际生产环境应该这样实现：
        String appId = "your_wechat_appid";
        String secret = "your_wechat_secret";
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, secret, code
        );
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            // 解析JSON响应
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);
            
            if (jsonNode.has("errcode")) {
                throw new BusinessException("WX_LOGIN_ERROR", "微信登录失败: " + jsonNode.get("errmsg").asText());
            }
            
            WxSessionResult result = new WxSessionResult();
            result.setOpenid(jsonNode.get("openid").asText());
            result.setSessionKey(jsonNode.get("session_key").asText());
            return result;
            
        } catch (Exception e) {
            log.error("调用微信接口失败: {}", e.getMessage(), e);
            throw new BusinessException("WX_API_ERROR", "调用微信接口失败");
        }
        */
    }
    
    /**
     * 解密微信用户信息
     */
    private WxUserInfo decryptUserInfo(String encryptedData, String iv, String sessionKey) {
        // TODO: 实现微信数据解密逻辑
        // 这需要使用AES解密算法
        WxUserInfo userInfo = new WxUserInfo();
        userInfo.setNickName("微信用户");
        userInfo.setAvatarUrl("");
        userInfo.setGender(0);
        return userInfo;
    }
    
    /**
     * 查找或创建微信用户
     */
    private User findOrCreateWxUser(String openid, WxUserInfo userInfo) {
        // 1. 根据openid查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        User existingUser = userMapper.selectOne(queryWrapper);
        
        if (existingUser != null) {
            // 用户已存在，直接返回
            return existingUser;
        }
        
        // 2. 用户不存在，创建新用户
        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setNickname(userInfo != null ? userInfo.getNickName() : "微信用户");
        newUser.setWechatNickname(userInfo != null ? userInfo.getNickName() : "微信用户");
        newUser.setAvatar(userInfo != null ? userInfo.getAvatarUrl() : "");
        newUser.setGender(userInfo != null ? userInfo.getGender() : 0);
        newUser.setRole("student");
        newUser.setAuthStatus(AuthStatusEnum.PENDING.getCode());
        newUser.setStatus(false);
        newUser.setCreatedTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        
        int insertResult = userMapper.insert(newUser);
        if (insertResult <= 0) {
            throw new BusinessException("CREATE_USER_FAILED", "创建用户失败");
        }
        
        return newUser;
    }
    
    @Override
    public RegisterResponse refreshToken(String oldToken) {
        log.info("开始处理Token刷新请求");
        
        // 1. 验证旧Token
        if (!JwtUtils.validateToken(oldToken)) {
            throw new BusinessException("无效的访问令牌");
        }
        
        // 2. 获取用户ID
        Integer userId = JwtUtils.getUserIdFromToken(oldToken);
        
        // 3. 检查旧Token是否在Redis中存在
        String oldTokenKey = String.format(RedisConstants.USER_JWT_TOKEN_KEY, userId);
        String storedOldToken = (String) redisUtils.get(oldTokenKey);
        if (!Objects.equals(storedOldToken, oldToken)) {
            throw new BusinessException("令牌已失效，无法刷新");
        }
        
        // 4. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 5. 检查用户状态
        if (user.getStatus() == null || user.getStatus()) {
            throw new BusinessException("账号已被冻结");
        }
        
        // 6. 生成新的Token
        String newToken = JwtUtils.generateToken(userId);
        
        // 7. 更新Redis中的Token
        storeTokenToRedis(userId, newToken);
        
        // 8. 构造响应
        RegisterResponse response = new RegisterResponse();
        response.setUserId(userId);
        response.setToken(newToken);
        response.setExpiresIn(JwtUtils.getExpirationTimeInSeconds());
        response.setAuthStatus(user.getAuthStatus());
        
        log.info("Token刷新成功，用户ID: {}", userId);
        return response;
    }
    

        
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        log.info("开始处理找回密码请求，账号：{}", forgotPasswordRequest.getAccount());
            
        // 1. 校验验证码
        validateVerifyCode(forgotPasswordRequest.getAccount(), forgotPasswordRequest.getVerifyCode());
            
        // 2. 根据账号查找用户
        User user = findUserByAccount(forgotPasswordRequest.getAccount());
            
        // 3. 更新密码
        String encodedPassword = SecurityUtils.encryptPassword(forgotPasswordRequest.getNewPassword());
        user.setPassword(encodedPassword);
        user.setUpdateTime(LocalDateTime.now());
            
        int updateResult = userMapper.updateById(user);
        if (updateResult <= 0) {
            throw new BusinessException("密码重置失败");
        }
            
        // 4. 清除验证码
        clearVerifyCode(forgotPasswordRequest.getAccount());
            
        // 5. 使所有已登录的 token 失效（可选）
        String tokenKey = String.format(RedisConstants.USER_JWT_TOKEN_KEY, user.getUserId());
        redisUtils.del(tokenKey);
            
        log.info("密码重置成功，用户 ID: {}", user.getUserId());
    }
        
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest changePasswordRequest, Integer userId) {
        log.info("开始处理修改密码请求，用户 ID: {}", userId);
            
        // 1. 根据 ID 查找用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
            
        // 2. 验证旧密码
        validatePassword(changePasswordRequest.getOldPassword(), user.getPassword());
            
        // 3. 更新密码
        String encodedPassword = SecurityUtils.encryptPassword(changePasswordRequest.getNewPassword());
        user.setPassword(encodedPassword);
        user.setUpdateTime(LocalDateTime.now());
            
        int updateResult = userMapper.updateById(user);
        if (updateResult <= 0) {
            throw new BusinessException("密码修改失败");
        }
            
        // 4. 使所有已登录的 token 失效（强制重新登录）
        String tokenKey = String.format(RedisConstants.USER_JWT_TOKEN_KEY, userId);
        redisUtils.del(tokenKey);
            
        log.info("密码修改成功，用户 ID: {}", userId);
    }
    
    /**
     * 微信session结果类
     */
    @Data
    private static class WxSessionResult {
        private String openid;
        private String sessionKey;
    }
    
    /**
     * 微信用户信息类
     */
    @Data
    private static class WxUserInfo {
        private String nickName;
        private String avatarUrl;
        private Integer gender;
        // 可以添加更多字段如city, province等
    }
}