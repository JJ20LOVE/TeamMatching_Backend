package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.utils.SecurityUtils;
import club.boyuan.official.teammatching.dto.request.user.AddSkillCertRequest;
import club.boyuan.official.teammatching.dto.request.user.UpdateNotificationSettingsRequest;
import club.boyuan.official.teammatching.dto.request.user.UpdateProfileRequest;
import club.boyuan.official.teammatching.dto.response.user.AddSkillCertResponse;
import club.boyuan.official.teammatching.dto.response.user.NotificationSettingsResponse;
import club.boyuan.official.teammatching.dto.response.user.NotificationSettingsSavedResponse;
import club.boyuan.official.teammatching.dto.response.user.SkillCertInfoResponse;
import club.boyuan.official.teammatching.dto.response.user.UserProfileResponse;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.SkillCertification;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.SkillCertificationMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.mq.dto.NotificationPushDto;
import club.boyuan.official.teammatching.mq.support.NotificationPreferenceUtils;
import club.boyuan.official.teammatching.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private SkillCertificationMapper skillCertificationMapper;
    
    @Autowired
    private FileResourceMapper fileResourceMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddSkillCertResponse addSkillCert(AddSkillCertRequest addSkillCertRequest, Integer userId) {
        log.info("开始添加技能认证，userId: {}, skillName: {}", userId, addSkillCertRequest.getSkillName());
        
        // 1. 验证证书文件是否存在
        Long certFileId = addSkillCertRequest.getCertFileId();
        FileResource certFile = fileResourceMapper.selectById(certFileId);
        if (certFile == null) {
            throw new BusinessException("证书文件不存在");
        }
        
        // 2. 验证文件是否属于当前用户
        if (!userId.equals(certFile.getUserId())) {
            throw new BusinessException("证书文件不属于当前用户");
        }
        
        // 3. 创建技能认证记录
        SkillCertification certification = new SkillCertification();
        certification.setUserId(userId);
        certification.setSkillName(addSkillCertRequest.getSkillName());
        certification.setCertName(addSkillCertRequest.getCertName());
        certification.setCertFileId(certFileId);
        certification.setIssueDate(addSkillCertRequest.getIssueDate());
        certification.setIssuer(addSkillCertRequest.getIssuer());
        certification.setCertNumber(addSkillCertRequest.getCertNumber());
        certification.setDescription(addSkillCertRequest.getDescription());
        certification.setStatus(1); // 正常状态
        certification.setAuditStatus(0); // 待审核
        certification.setCreatedTime(LocalDateTime.now());
        certification.setUpdateTime(LocalDateTime.now());
        
        int insertResult = skillCertificationMapper.insert(certification);
        if (insertResult <= 0) {
            throw new BusinessException("添加技能认证失败");
        }
        
        // 4. 更新文件的关联状态
        certFile.setTargetType(2); // 2-技能认证证书
        certFile.setTargetId(certification.getCertId());
        fileResourceMapper.updateById(certFile);
        
        // 5. 构造响应
        AddSkillCertResponse response = new AddSkillCertResponse();
        response.setCertId(certification.getCertId());
        response.setMessage("添加成功，等待审核");
        response.setAuditStatus(0);
        
        log.info("技能认证添加成功，certId: {}", certification.getCertId());
        return response;
    }
    
    @Override
    public java.util.List<SkillCertInfoResponse> getSkillCertList(Integer userId) {
        log.info("开始获取用户技能认证列表，userId: {}", userId);
        
        // 1. 查询用户的技能认证列表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SkillCertification> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(SkillCertification::getUserId, userId)
                   .eq(SkillCertification::getStatus, 1) // 只查询正常状态的
                   .orderByDesc(SkillCertification::getCreatedTime);
        
        java.util.List<SkillCertification> certifications = skillCertificationMapper.selectList(queryWrapper);
        
        // 2. 转换为响应对象
        java.util.List<SkillCertInfoResponse> responseList = new java.util.ArrayList<>();
        if (certifications != null && !certifications.isEmpty()) {
            for (SkillCertification cert : certifications) {
                SkillCertInfoResponse response = new SkillCertInfoResponse();
                response.setCertId(cert.getCertId());
                response.setSkillName(cert.getSkillName());
                response.setCertName(cert.getCertName());
                response.setIssueDate(cert.getIssueDate());
                response.setIssuer(cert.getIssuer());
                response.setCertNumber(cert.getCertNumber());
                response.setDescription(cert.getDescription());
                response.setAuditStatus(cert.getAuditStatus());
                response.setAuditTime(cert.getAuditTime());
                response.setCreatedTime(cert.getCreatedTime());
                
                // 3. 查询证书文件信息
                if (cert.getCertFileId() != null) {
                    FileResource certFile = fileResourceMapper.selectById(cert.getCertFileId());
                    if (certFile != null) {
                        SkillCertInfoResponse.CertFileInfo fileInfo = 
                            new SkillCertInfoResponse.CertFileInfo();
                        fileInfo.setFileId(certFile.getFileId());
                        fileInfo.setFileName(certFile.getFileName());
                        fileInfo.setFileUrl(certFile.getFileUrl());
                        fileInfo.setFileSize(certFile.getFileSize());
                        response.setCertFile(fileInfo);
                    }
                }
                
                responseList.add(response);
            }
        }
        
        log.info("技能认证列表获取成功，userId: {}, count: {}", userId, responseList.size());
        return responseList;
    }

    @Override
    public NotificationSettingsResponse getNotificationSettings(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        NotificationSettingsResponse response = new NotificationSettingsResponse();
        response.setMessageNotify(NotificationPreferenceUtils.isChannelEnabled(user.getMessageNotify()));
        response.setProjectUpdateNotify(NotificationPreferenceUtils.isChannelEnabled(user.getProjectUpdateNotify()));
        response.setInvitationNotify(NotificationPreferenceUtils.isChannelEnabled(user.getInvitationNotify()));
        response.setSystemNotify(NotificationPreferenceUtils.isChannelEnabled(user.getSystemNotify()));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationSettingsSavedResponse updateNotificationSettings(Integer userId, UpdateNotificationSettingsRequest request) {
        if (request.getMessageNotify() == null && request.getProjectUpdateNotify() == null
                && request.getInvitationNotify() == null && request.getSystemNotify() == null) {
            throw new BusinessException("请至少提供一项通知设置");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        boolean updated = new LambdaUpdateChainWrapper<>(userMapper)
                .eq(User::getUserId, userId)
                .set(request.getMessageNotify() != null, User::getMessageNotify, request.getMessageNotify())
                .set(request.getProjectUpdateNotify() != null, User::getProjectUpdateNotify, request.getProjectUpdateNotify())
                .set(request.getInvitationNotify() != null, User::getInvitationNotify, request.getInvitationNotify())
                .set(request.getSystemNotify() != null, User::getSystemNotify, request.getSystemNotify())
                .set(true, User::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new BusinessException("更新通知设置失败");
        }
        NotificationSettingsSavedResponse response = new NotificationSettingsSavedResponse();
        response.setMessage("设置已保存");

        pushSettingsSync(userId);
        return response;
    }

    private void pushSettingsSync(Integer userId) {
        try {
            NotificationSettingsResponse snapshot = getNotificationSettings(userId);
            NotificationPushDto push = new NotificationPushDto();
            push.setPushType("SETTINGS_SYNC");
            push.setCreateTimeMillis(System.currentTimeMillis());
            push.setSettings(snapshot);
            messagingTemplate.convertAndSend("/topic/notify/" + userId, push);
        } catch (Exception e) {
            log.warn("通知设置 WebSocket 同步失败 userId={}", userId, e);
        }
    }
}