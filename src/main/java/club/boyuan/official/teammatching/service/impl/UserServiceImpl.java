package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.utils.SecurityUtils;
import club.boyuan.official.teammatching.dto.request.user.UpdateProfileRequest;
import club.boyuan.official.teammatching.dto.response.user.UserProfileResponse;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FileResourceMapper fileResourceMapper;
    
    @Override
    public UserProfileResponse getUserProfile(Integer userId) {
        log.info("开始获取用户资料，userId: {}", userId);
        
        // 1. 根据 ID 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 构建响应对象
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        
        // 3. 处理头像信息
        if (user.getAvatarFileId() != null) {
            FileResource avatarFile = fileResourceMapper.selectById(user.getAvatarFileId());
            if (avatarFile != null) {
                UserProfileResponse.AvatarFileDTO avatarFileDTO = 
                    new UserProfileResponse.AvatarFileDTO();
                avatarFileDTO.setFileId(avatarFile.getFileId());
                avatarFileDTO.setFileName(avatarFile.getFileName());
                avatarFileDTO.setFileUrl(avatarFile.getFileUrl());
                avatarFileDTO.setFileSize(avatarFile.getFileSize());
                response.setAvatarFile(avatarFileDTO);
            }
        }
        
        // 4. 手机号脱敏处理
        if (user.getPhone() != null && user.getPhone().length() == 11) {
            response.setPhone(user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        
        log.info("用户资料获取成功，userId: {}", userId);
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Integer userId, UpdateProfileRequest request) {
        log.info("开始更新用户资料，userId: {}", userId);
        
        // 1. 根据 ID 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 验证头像文件是否存在（如果提供了 avatarFileId）
        if (request.getAvatarFileId() != null) {
            FileResource avatarFile = fileResourceMapper.selectById(request.getAvatarFileId());
            if (avatarFile ==  null) {
                throw new BusinessException("头像文件不存在");
            }
        }
        
        // 3. 使用链式编程构建动态更新并执行
        boolean updateSuccess = new LambdaUpdateChainWrapper<>(userMapper)
                .eq(User::getUserId, userId)
                .set(request.getNickname() != null, User::getNickname, request.getNickname())
                .set(request.getAvatarFileId() != null, User::getAvatarFileId, request.getAvatarFileId())
                .set(request.getGender() != null, User::getGender, request.getGender())
                .set(request.getBirthday() != null, User::getBirthday, request.getBirthday())
                .set(request.getMajor() != null, User::getMajor, request.getMajor())
                .set(request.getGrade() != null, User::getGrade, request.getGrade())
                .set(request.getTechStack() != null, User::getTechStack, request.getTechStack())
                .set(request.getPersonalIntro() != null, User::getPersonalIntro, request.getPersonalIntro())
                .set(request.getAwardExperience() != null, User::getAwardExperience, request.getAwardExperience())
                .set(true, User::getUpdateTime, LocalDateTime.now())
                // 4. 执行更新
                .update();
        
        if (!updateSuccess) {
            throw new BusinessException("更新用户资料失败");
        }
        
        log.info("用户资料更新成功，userId: {}", userId);
    }
}