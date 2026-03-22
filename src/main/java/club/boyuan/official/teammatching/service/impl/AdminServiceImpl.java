package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.admin.AuditRequest;
import club.boyuan.official.teammatching.dto.request.admin.AuditVerifyRequest;
import club.boyuan.official.teammatching.dto.request.admin.ContentAuditRequest;
import club.boyuan.official.teammatching.dto.response.admin.*;
import club.boyuan.official.teammatching.entity.*;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
import club.boyuan.official.teammatching.mapper.*;
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
    private final ProjectMapper projectMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommentMapper commentMapper;
    private final TeamMemberMapper teamMemberMapper;

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

    @Override
    public ContentAuditResponse getAuditContents(ContentAuditRequest request) {
        String type = request.getType();
        List<ContentAuditResponse.ContentItemDTO> contentItems;

        if ("project".equalsIgnoreCase(type)) {
            List<Project> projects = getPendingProjects();
            contentItems = buildContentItems(
                    projects,
                    Project::getPublisherUserId,
                    project -> buildProjectItem(project, "project")
            );
        } else if ("post".equalsIgnoreCase(type)) {
            List<CommunityPost> posts = getPendingPosts();
            contentItems = buildContentItems(
                    posts,
                    CommunityPost::getUserId,
                    post -> buildPostItem(post, "post")
            );
        } else if ("comment".equalsIgnoreCase(type)) {
            List<Comment> comments = getPendingComments();
            contentItems = buildContentItems(
                    comments,
                    Comment::getUserId,
                    comment -> buildCommentItem(comment, "comment")
            );
        } else {
            contentItems = new ArrayList<>();
        }

        return new ContentAuditResponse(contentItems);
    }

    /**
     * 查询待审核的项目
     */
    private List<Project> getPendingProjects() {
        LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<>();
        query.eq(Project::getAuditStatus, 0)
                .orderByDesc(Project::getReleaseTime);
        return projectMapper.selectList(query);
    }

    /**
     * 查询待审核的帖子
     */
    private List<CommunityPost> getPendingPosts() {
        LambdaQueryWrapper<CommunityPost> query = new LambdaQueryWrapper<>();
        query.eq(CommunityPost::getStatus, 0)
                .orderByDesc(CommunityPost::getCreatedTime);
        return communityPostMapper.selectList(query);
    }

    /**
     * 查询待审核的评论
     */
    private List<Comment> getPendingComments() {
        LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<>();
        query.eq(Comment::getStatus, 0)
                .orderByDesc(Comment::getCreatedTime);
        return commentMapper.selectList(query);
    }

    /**
     * 通用方法：构建内容审核项列表
     * @param items 待审核的内容列表
     * @param userIdExtractor 提取用户ID的函数
     * @param itemBuilder 构建ContentItemDTO的函数
     * @param <T> 内容类型
     * @return 内容审核项列表
     */
    private <T> List<ContentAuditResponse.ContentItemDTO> buildContentItems(
            List<T> items,
            java.util.function.Function<T, Integer> userIdExtractor,
            java.util.function.Function<T, ContentAuditResponse.ContentItemDTO> itemBuilder) {

        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        Map<Integer, User> userMap = batchQueryUsers(items, userIdExtractor);

        // 组装数据
        List<ContentAuditResponse.ContentItemDTO> contentItems = new ArrayList<>();
        for (T item : items) {
            ContentAuditResponse.ContentItemDTO contentItem = itemBuilder.apply(item);

            // 设置发布者信息
            Integer userId = userIdExtractor.apply(item);
            User publisher = userMap.get(userId);
            if (publisher != null) {
                contentItem.setPublisher(buildPublisherDTO(publisher));
            }

            contentItems.add(contentItem);
        }

        return contentItems;
    }

    /**
     * 批量查询用户信息
     */
    private <T> Map<Integer, User> batchQueryUsers(
            List<T> items,
            java.util.function.Function<T, Integer> userIdExtractor) {

        List<Integer> userIds = items.stream()
                .map(userIdExtractor)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(User::getUserId, userIds);
        List<User> users = userMapper.selectList(userQuery);

        return users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
    }

    /**
     * 构建项目审核项
     */
    private ContentAuditResponse.ContentItemDTO buildProjectItem(Project project, String type) {
        ContentAuditResponse.ContentItemDTO item = new ContentAuditResponse.ContentItemDTO();
        item.setContentId(project.getProjectId());
        item.setContentType(type);
        item.setTitle(project.getName());
        item.setContent(project.getProjectIntro());
        item.setPublishTime(project.getReleaseTime());
        item.setStatus(project.getAuditStatus());
        return item;
    }

    /**
     * 构建帖子审核项
     */
    private ContentAuditResponse.ContentItemDTO buildPostItem(CommunityPost post, String type) {
        ContentAuditResponse.ContentItemDTO item = new ContentAuditResponse.ContentItemDTO();
        item.setContentId(post.getPostId());
        item.setContentType(type);
        item.setTitle(post.getTitle());
        item.setContent(post.getContent());
        item.setPublishTime(post.getCreatedTime());
        item.setStatus(post.getStatus());
        return item;
    }

    /**
     * 构建评论审核项
     */
    private ContentAuditResponse.ContentItemDTO buildCommentItem(Comment comment, String type) {
        ContentAuditResponse.ContentItemDTO item = new ContentAuditResponse.ContentItemDTO();
        item.setContentId(comment.getCommentId());
        item.setContentType(type);
        item.setTitle(null); // 评论没有标题
        item.setContent(comment.getContent());
        item.setPublishTime(comment.getCreatedTime());
        item.setStatus(comment.getStatus());
        return item;
    }

    /**
     * 构建发布者DTO
     */
    private ContentAuditResponse.PublisherDTO buildPublisherDTO(User user) {
        ContentAuditResponse.PublisherDTO publisherDTO = new ContentAuditResponse.PublisherDTO();
        publisherDTO.setUserId(user.getUserId());
        publisherDTO.setUsername(user.getUsername());
        publisherDTO.setNickname(user.getNickname());
        return publisherDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentVerifyResponse verifyContent(String contentType, Integer contentId, AuditVerifyRequest request) {
        // 获取当前管理员ID
        Integer auditorUserId = UserContextUtil.getCurrentUserId();
        if (auditorUserId == null) {
            // TODO: 临时用于测试，生产环境需要移除
            auditorUserId = 1; // 使用默认管理员ID进行测试
        }

        // 验证审核结果的有效性（1-通过 2-驳回）
        if (request.getResult() == null || (request.getResult() != 1 && request.getResult() != 2)) {
            throw new BusinessException("审核结果无效，必须为1（通过）或2（驳回）");
        }

        LocalDateTime now = LocalDateTime.now();

        // 根据内容类型进行不同的处理
        if ("project".equalsIgnoreCase(contentType)) {
            // 审核项目
            Project project = projectMapper.selectById(contentId);
            if (project == null) {
                throw new ResourceNotFoundException("项目不存在");
            }

            LambdaUpdateWrapper<Project> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Project::getProjectId, contentId)
                    .set(Project::getAuditStatus, request.getResult())
                    .set(Project::getAuditTime, now)
                    .set(Project::getAuditorUserId, auditorUserId)
                    .set(Project::getRemark, request.getReason());
            projectMapper.update(null, updateWrapper);

        } else if ("post".equalsIgnoreCase(contentType)) {
            // 审核帖子
            CommunityPost post = communityPostMapper.selectById(contentId);
            if (post == null) {
                throw new ResourceNotFoundException("帖子不存在");
            }

            // 对于帖子，status 字段：1-正常 0-删除 2-违规下架
            // 如果审核通过（result=1），设置 status=1（正常）
            // 如果审核驳回（result=2），设置 status=2（违规下架）
            Integer newStatus = request.getResult() == 1 ? 1 : 2;

            LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommunityPost::getPostId, contentId)
                    .set(CommunityPost::getStatus, newStatus)
                    .set(CommunityPost::getUpdateTime, now);
            communityPostMapper.update(null, updateWrapper);

        } else if ("comment".equalsIgnoreCase(contentType)) {
            // 审核评论
            Comment comment = commentMapper.selectById(contentId);
            if (comment == null) {
                throw new ResourceNotFoundException("评论不存在");
            }

            // 对于评论，status 字段：1-正常 0-删除
            // 如果审核通过（result=1），设置 status=1（正常）
            // 如果审核驳回（result=2），设置 status=0（删除）
            Integer newStatus = request.getResult() == 1 ? 1 : 0;

            LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Comment::getCommentId, contentId)
                    .set(Comment::getStatus, newStatus)
                    .set(Comment::getUpdateTime, now);
            commentMapper.update(null, updateWrapper);

        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }

        return new ContentVerifyResponse("审核完成");
    }

    @Override
    public StatisticsResponse getStatistics() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 1. 计算日活跃用户数 (DAU) - 今天登录过的用户
        LambdaQueryWrapper<User> dauQuery = new LambdaQueryWrapper<>();
        dauQuery.between(User::getLastLoginTime, todayStart, todayEnd);
        Integer dau = Math.toIntExact(userMapper.selectCount(dauQuery));

        // 2. 计算今日新增用户数
        LambdaQueryWrapper<User> newUsersQuery = new LambdaQueryWrapper<>();
        newUsersQuery.between(User::getCreatedTime, todayStart, todayEnd);
        Integer newUsers = Math.toIntExact(userMapper.selectCount(newUsersQuery));

        // 3. 计算今日发布的项目数
        LambdaQueryWrapper<Project> projectsQuery = new LambdaQueryWrapper<>();
        projectsQuery.between(Project::getReleaseTime, todayStart, todayEnd);
        Integer projectsPublished = Math.toIntExact(projectMapper.selectCount(projectsQuery));

        // 4. 计算组建的团队数 (统计今日加入且状态为"在队"的团队成员数)
        LambdaQueryWrapper<TeamMember> teamsQuery = new LambdaQueryWrapper<>();
        teamsQuery.between(TeamMember::getJoinTime, todayStart, todayEnd)
                .eq(TeamMember::getStatus, 0); // 0-在队
        Integer teamsFormed = Math.toIntExact(teamMemberMapper.selectCount(teamsQuery));

        // 5. 计算内容审核通过率
        // 统计所有已审核的项目、帖子、评论的通过率
        double contentAuditPassRate = calculateContentAuditPassRate();

        return new StatisticsResponse(dau, newUsers, projectsPublished, teamsFormed, contentAuditPassRate);
    }

    /**
     * 计算内容审核通过率
     * 统计项目、帖子、评论的审核通过率
     */
    private double calculateContentAuditPassRate() {
        // 统计项目审核情况
        LambdaQueryWrapper<Project> projectAuditedQuery = new LambdaQueryWrapper<>();
        projectAuditedQuery.in(Project::getAuditStatus, 1, 2); // 1-通过, 2-驳回
        long totalProjectsAudited = projectMapper.selectCount(projectAuditedQuery);

        LambdaQueryWrapper<Project> projectPassedQuery = new LambdaQueryWrapper<>();
        projectPassedQuery.eq(Project::getAuditStatus, 1); // 1-通过
        long projectsPassed = projectMapper.selectCount(projectPassedQuery);

        // 统计帖子审核情况 (status: 1-正常, 2-违规下架)
        LambdaQueryWrapper<CommunityPost> postAuditedQuery = new LambdaQueryWrapper<>();
        postAuditedQuery.in(CommunityPost::getStatus, 1, 2); // 1-正常, 2-违规下架
        long totalPostsAudited = communityPostMapper.selectCount(postAuditedQuery);

        LambdaQueryWrapper<CommunityPost> postPassedQuery = new LambdaQueryWrapper<>();
        postPassedQuery.eq(CommunityPost::getStatus, 1); // 1-正常
        long postsPassed = communityPostMapper.selectCount(postPassedQuery);

        // 统计评论审核情况 (status: 1-正常, 0-删除)
        LambdaQueryWrapper<Comment> commentAuditedQuery = new LambdaQueryWrapper<>();
        commentAuditedQuery.in(Comment::getStatus, 0, 1); // 0-删除, 1-正常
        long totalCommentsAudited = commentMapper.selectCount(commentAuditedQuery);

        LambdaQueryWrapper<Comment> commentPassedQuery = new LambdaQueryWrapper<>();
        commentPassedQuery.eq(Comment::getStatus, 1); // 1-正常
        long commentsPassed = commentMapper.selectCount(commentPassedQuery);

        // 计算总体通过率
        long totalAudited = totalProjectsAudited + totalPostsAudited + totalCommentsAudited;
        long totalPassed = projectsPassed + postsPassed + commentsPassed;

        if (totalAudited == 0) {
            return 0.0;
        }

        // 返回通过率，保留两位小数
        return Math.round((double) totalPassed / totalAudited * 100) / 100.0;
    }
}