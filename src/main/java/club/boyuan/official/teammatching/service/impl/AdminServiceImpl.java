package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.response.admin.AuditListResponse;
import club.boyuan.official.teammatching.dto.response.admin.AuditVerifyResponse;
import club.boyuan.official.teammatching.entity.AuthMaterial;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.AuthMaterialMapper;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AuthMaterialMapper authMaterialMapper;
    private final FileResourceMapper fileResourceMapper;

    @Override
    public AuditListResponse getPendingAuthList(AuditRequest request) {
        // 创建分页对象
        Integer page = request.getPage();
        Integer size = request.getSize();
        Page<User> userPage = new Page<>(page, size);

        // 查询待审核用户（authStatus = 0）
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getAuthStatus, 0)
                .orderByAsc(User::getCreatedTime);

        Page<User> resultPage = userMapper.selectPage(userPage, userQuery);

        // 获取用户ID列表
        List<Integer> userIds = resultPage.getRecords().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 查询所有相关的认证材料
        Map<Integer, List<AuthMaterial>> materialsMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<AuthMaterial> materialQuery = new LambdaQueryWrapper<>();
            materialQuery.in(AuthMaterial::getUserId, userIds);
            List<AuthMaterial> materials = authMaterialMapper.selectList(materialQuery);

            // 按用户ID分组
            materialsMap = materials.stream()
                    .collect(Collectors.groupingBy(AuthMaterial::getUserId));
        }

        // 获取所有文件ID
        List<Long> fileIds = materialsMap.values().stream()
                .flatMap(List::stream)
                .map(AuthMaterial::getFileId)
                .distinct()
                .collect(Collectors.toList());

        // 查询所有相关文件
        Map<Long, FileResource> fileMap = new java.util.HashMap<>();
        if (!fileIds.isEmpty()) {
            LambdaQueryWrapper<FileResource> fileQuery = new LambdaQueryWrapper<>();
            fileQuery.in(FileResource::getFileId, fileIds);
            List<FileResource> files = fileResourceMapper.selectList(fileQuery);
            fileMap = files.stream()
                    .collect(Collectors.toMap(FileResource::getFileId, f -> f));
        }

        // 组装返回数据
        List<AuditListResponse.AuthItemDTO> authItems = new ArrayList<>();
        for (User user : resultPage.getRecords()) {
            AuditListResponse.AuthItemDTO authItem = new AuditListResponse.AuthItemDTO();
            authItem.setAuthId(user.getUserId());
            authItem.setStudentId(user.getStudentId());
            authItem.setRealName(user.getUsername());
            authItem.setMajor(user.getMajor());
            authItem.setGrade(user.getGrade());
            authItem.setEmail(user.getEmail());
            authItem.setApplyTime(user.getCreatedTime());

            // 组装材料列表
            List<AuditListResponse.MaterialDTO> materialDTOs = new ArrayList<>();
            List<AuthMaterial> userMaterials = materialsMap.getOrDefault(user.getUserId(), new ArrayList<>());

            for (AuthMaterial material : userMaterials) {
                AuditListResponse.MaterialDTO materialDTO = new AuditListResponse.MaterialDTO();
                materialDTO.setMaterialId(material.getMaterialId());
                materialDTO.setMaterialType(material.getMaterialType());

                // 组装文件信息
                FileResource file = fileMap.get(material.getFileId());
                if (file != null) {
                    AuditListResponse.FileInfoDTO fileInfo = new AuditListResponse.FileInfoDTO();
                    fileInfo.setFileId(file.getFileId());
                    fileInfo.setFileName(file.getFileName());
                    fileInfo.setFileUrl(file.getFileUrl());
                    fileInfo.setFileSize(file.getFileSize());
                    materialDTO.setFileInfo(fileInfo);
                }

                materialDTOs.add(materialDTO);
            }

            authItem.setMaterials(materialDTOs);
            authItems.add(authItem);
        }

        // 返回结果
        return new AuditListResponse(
                (int) resultPage.getTotal(),
                page,
                size,
                authItems
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditVerifyResponse auditAuth(Integer authId, AuditVerifyRequest request) {
        // 获取当前管理员ID
        Integer auditorUserId = UserContextUtil.getCurrentUserId();
        if (auditorUserId == null) {
            // TODO: 临时用于测试，生产环境需要移除
            auditorUserId = 1; // 使用默认管理员ID进行测试
        }

        // 查询用户是否存在
        User user = userMapper.selectById(authId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 验证审核结果的有效性（1-通过 2-驳回）
        if (request.getResult() == null || (request.getResult() != 1 && request.getResult() != 2)) {
            throw new BusinessException("审核结果无效，必须为1（通过）或2（驳回）");
        }

        // 更新用户的认证状态
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<User> userUpdateWrapper = new LambdaUpdateWrapper<>();
        userUpdateWrapper.eq(User::getUserId, authId)
                .set(User::getAuthStatus, request.getResult())
                .set(User::getAuditTime, now)
                .set(User::getAuditorUserId, auditorUserId)
                .set(User::getRemark, request.getRemark());
        userMapper.update(null, userUpdateWrapper);

        // 如果有材料审核结果，更新材料的审核状态
        if (request.getMaterialResults() != null && !request.getMaterialResults().isEmpty()) {
            for (AuditVerifyRequest.MaterialResultDTO materialResult : request.getMaterialResults()) {
                if (materialResult.getMaterialId() != null) {
                    // 验证材料是否属于该用户
                    AuthMaterial material = authMaterialMapper.selectById(materialResult.getMaterialId());
                    if (material != null && material.getUserId().equals(authId)) {
                        // 更新材料审核状态
                        LambdaUpdateWrapper<AuthMaterial> materialUpdateWrapper = new LambdaUpdateWrapper<>();
                        materialUpdateWrapper.eq(AuthMaterial::getMaterialId, materialResult.getMaterialId())
                                .set(AuthMaterial::getAuditStatus, materialResult.getResult() != null ? materialResult.getResult() : request.getResult())
                                .set(AuthMaterial::getAuditTime, now)
                                .set(AuthMaterial::getAuditorUserId, auditorUserId)
                                .set(AuthMaterial::getRemark, materialResult.getRemark());
                        authMaterialMapper.update(null, materialUpdateWrapper);
                    }
                }
            }
        }

        // 返回审核结果
        return new AuditVerifyResponse("审核完成", request.getResult());
    }
}