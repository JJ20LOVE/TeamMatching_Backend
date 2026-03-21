package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostPageResult;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.entity.Comment;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.LikeRecord;
import club.boyuan.official.teammatching.entity.PostImageRelation;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.CommentMapper;
import club.boyuan.official.teammatching.mapper.CommunityPostMapper;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.LikeRecordMapper;
import club.boyuan.official.teammatching.mapper.PostImageRelationMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.CommunityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社区服务实现
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final FileResourceMapper fileResourceMapper;
    private final PostImageRelationMapper postImageRelationMapper;

    public CommunityServiceImpl(UserMapper userMapper,
                                CommentMapper commentMapper,
                                LikeRecordMapper likeRecordMapper,
                                FileResourceMapper fileResourceMapper,
                                PostImageRelationMapper postImageRelationMapper) {
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.fileResourceMapper = fileResourceMapper;
        this.postImageRelationMapper = postImageRelationMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Number createNewPost(CreatePostRequest request, Integer userId) {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setSection(request.getSection());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(0); // 发帖后进入审核态
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setCreatedTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        boolean saved = this.save(post);
        if (!saved || post.getPostId() == null) {
            throw new BusinessException("帖子发布失败");
        }

        bindPostImages(post.getPostId(), request.getImages());
        return post.getPostId();
    }

    @Override
    public PostPageResult queryPostList(CommunityQueryRequest request, Integer userId) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();

        if (request.getSection() != null) {
            wrapper.eq(CommunityPost::getSection, request.getSection().getCode());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, request.getKeyword())
                    .or().like(CommunityPost::getContent, request.getKeyword()));
        }

        wrapper.eq(CommunityPost::getStatus, 1); // 仅展示审核通过帖子

        if (request.getType() != null) {
            switch (request.getType()) {
                case LATEST -> wrapper.orderByDesc(CommunityPost::getCreatedTime);
                case HOTTEST -> wrapper.orderByDesc(CommunityPost::getViewCount)
                        .orderByDesc(CommunityPost::getLikeCount);
                case RECOMMEND -> wrapper.orderByDesc(CommunityPost::getIsTop)
                        .orderByDesc(CommunityPost::getIsEssence)
                        .orderByDesc(CommunityPost::getCreatedTime);
            }
        } else {
            wrapper.orderByDesc(CommunityPost::getCreatedTime);
        }

        long current = Optional.ofNullable(request.getPage()).map(Integer::longValue).orElse(1L);
        long size = Optional.ofNullable(request.getSize()).map(Integer::longValue).orElse(10L);

        Page<CommunityPost> pageParam = new Page<>(current, size);
        Page<CommunityPost> postPage = this.page(pageParam, wrapper);

        PostPageResult result = new PostPageResult();
        result.setCurrent(postPage.getCurrent());
        result.setSize(postPage.getSize());
        result.setTotal(postPage.getTotal());
        result.setPages(postPage.getPages());

        List<CommunityPost> posts = postPage.getRecords();
        if (posts == null || posts.isEmpty()) {
            result.setRecords(new ArrayList<>());
            return result;
        }

        List<Integer> userIds = posts.stream()
                .map(CommunityPost::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));
        Map<Long, String> avatarUrlMap = buildAvatarUrlMap(users);
        Map<Integer, List<String>> postImageMap = buildPostImageMap(posts);

        List<PostListResponse.CommunityPostItem> items = posts.stream().map(post -> {
            PostListResponse.CommunityPostItem item = new PostListResponse.CommunityPostItem();
            item.setPostId(post.getPostId());
            item.setSection(post.getSection());
            item.setTitle(post.getTitle());
            item.setContent(post.getContent());
            item.setViewCount(post.getViewCount());
            item.setLikeCount(post.getLikeCount());
            item.setCommentCount(post.getCommentCount());
            item.setIsTop(post.getIsTop());
            item.setIsEssence(post.getIsEssence());
            item.setCreatedTime(post.getCreatedTime());
            User user = userMap.get(post.getUserId());
            item.setUserInfo(getUserInfo(user, avatarUrlMap));
            item.setImages(postImageMap.getOrDefault(post.getPostId(), new ArrayList<>()));
            return item;
        }).collect(Collectors.toList());

        result.setRecords(items);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNewComment(Long postId, CreateCommentRequest request, Integer userId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException("帖子ID错误");
        }
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }

        CommunityPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子已不存在");
        }
        if (!Objects.equals(post.getStatus(), 1)) {
            throw new BusinessException("帖子正在审核中，暂不可评论");
        }

        Integer parentId = request.getParentId();
        if (parentId != null && parentId > 0) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException("回复的评论不存在");
            }
            if (!Objects.equals(parent.getPostId(), Math.toIntExact(postId))) {
                throw new BusinessException("父评论不属于当前帖子");
            }
        } else {
            parentId = null;
        }

        Comment comment = new Comment();
        comment.setPostId(Math.toIntExact(postId));
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreatedTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        int insertResult = commentMapper.insert(comment);
        if (insertResult <= 0 || comment.getCommentId() == null) {
            throw new BusinessException("评论发布失败");
        }

        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        post.setUpdateTime(LocalDateTime.now());
        this.updateById(post);

        return comment.getCommentId().longValue();
    }

    @Transactional
    @Override
    public LikeResponse toggleLikeStatus(LikeRequest request, Integer userId) {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getTargetType() == null || request.getTargetType() <= 0) {
            throw new BusinessException("目标类型错误");
        }
        if (request.getTargetId() == null || request.getTargetId() <= 0) {
            throw new BusinessException("目标ID错误");
        }

        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, request.getTargetType())
                .eq(LikeRecord::getTargetId, request.getTargetId());
        LikeRecord exist = likeRecordMapper.selectOne(wrapper);

        ToggleResult toggleResult = applyLikeToggle(exist, userId, request.getTargetType(), request.getTargetId());
        Integer likeCount;

        if (request.getTargetType() == 1) {
            CommunityPost post = this.getById(request.getTargetId());
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }
            if (!Objects.equals(post.getStatus(), 1)) {
                throw new BusinessException("帖子正在审核中，暂不可点赞");
            }
            int current = post.getLikeCount() == null ? 0 : post.getLikeCount();
            post.setLikeCount(Math.max(0, current + toggleResult.delta()));
            post.setUpdateTime(LocalDateTime.now());
            this.updateById(post);
            likeCount = post.getLikeCount();
        } else if (request.getTargetType() == 2) {
            Comment comment = commentMapper.selectById(request.getTargetId());
            if (comment == null) {
                throw new BusinessException("评论不存在");
            }
            if (!Objects.equals(comment.getStatus(), 1)) {
                throw new BusinessException("评论已不可点赞");
            }
            int current = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
            comment.setLikeCount(Math.max(0, current + toggleResult.delta()));
            comment.setUpdateTime(LocalDateTime.now());
            commentMapper.updateById(comment);
            likeCount = comment.getLikeCount();
        } else {
            throw new BusinessException("目标类型错误");
        }

        return new LikeResponse(toggleResult.isLiked(), likeCount);
    }

    private ToggleResult applyLikeToggle(LikeRecord exist, Integer userId, Integer targetType, Integer targetId) {
        if (exist == null) {
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            record.setCreatedTime(LocalDateTime.now());
            likeRecordMapper.insert(record);
            return new ToggleResult(true, 1);
        }
        likeRecordMapper.deleteById(exist.getLikeId());
        return new ToggleResult(false, -1);
    }

    private record ToggleResult(boolean isLiked, int delta) {
    }

    private void bindPostImages(Integer postId, List<String> images) {
        if (postId == null || images == null || images.isEmpty()) {
            return;
        }

        Set<String> uniqueUrls = images.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueUrls.isEmpty()) {
            return;
        }

        int sort = 0;
        for (String url : uniqueUrls) {
            LambdaQueryWrapper<FileResource> fileQuery = new LambdaQueryWrapper<>();
            fileQuery.eq(FileResource::getFileUrl, url)
                    .eq(FileResource::getIsDeleted, false)
                    .eq(FileResource::getTargetType, 3)
                    .last("LIMIT 1");
            List<FileResource> candidates = fileResourceMapper.selectList(fileQuery);
            if (candidates == null || candidates.isEmpty()) {
                throw new BusinessException("图片资源不存在: " + url);
            }
            FileResource fileResource = candidates.get(0);

            fileResource.setTargetType(3);
            fileResource.setTargetId(postId);
            fileResource.setIsTemp(false);
            fileResource.setUpdateTime(LocalDateTime.now());
            fileResourceMapper.updateById(fileResource);

            PostImageRelation relation = new PostImageRelation();
            relation.setPostId(postId);
            relation.setFileId(fileResource.getFileId());
            relation.setSortOrder(sort++);
            relation.setCreatedTime(LocalDateTime.now());
            postImageRelationMapper.insert(relation);
        }
    }

    private Map<Integer, List<String>> buildPostImageMap(List<CommunityPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> postIds = posts.stream()
                .map(CommunityPost::getPostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<PostImageRelation> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(PostImageRelation::getPostId, postIds)
                .orderByAsc(PostImageRelation::getSortOrder)
                .orderByAsc(PostImageRelation::getRelationId);
        List<PostImageRelation> relations = postImageRelationMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> fileIds = relations.stream()
                .map(PostImageRelation::getFileId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (fileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<FileResource> files = fileResourceMapper.selectBatchIds(fileIds);
        Map<Long, String> fileUrlMap = files.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .filter(f -> StringUtils.hasText(f.getFileUrl()))
                .collect(Collectors.toMap(FileResource::getFileId, FileResource::getFileUrl, (a, b) -> a));

        Map<Integer, List<String>> result = new HashMap<>();
        for (PostImageRelation relation : relations) {
            String url = fileUrlMap.get(relation.getFileId());
            if (!StringUtils.hasText(url)) {
                continue;
            }
            result.computeIfAbsent(relation.getPostId(), key -> new ArrayList<>()).add(url);
        }
        return result;
    }

    private Map<Long, String> buildAvatarUrlMap(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> avatarFileIds = users.stream()
                .map(User::getAvatarFileId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (avatarFileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<FileResource> avatarFiles = fileResourceMapper.selectBatchIds(avatarFileIds);
        return avatarFiles.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .filter(f -> StringUtils.hasText(f.getFileUrl()))
                .collect(Collectors.toMap(FileResource::getFileId, FileResource::getFileUrl, (a, b) -> a));
    }

    @NonNull
    private static PostListResponse.UserInfo getUserInfo(User user, Map<Long, String> avatarUrlMap) {
        PostListResponse.UserInfo userInfo = new PostListResponse.UserInfo();
        if (user != null) {
            userInfo.setUserId(user.getUserId());
            userInfo.setNickname(user.getNickname() != null ? user.getNickname() : "");
            String avatarUrl = user.getAvatarFileId() == null
                    ? ""
                    : avatarUrlMap.getOrDefault(user.getAvatarFileId(), "");
            userInfo.setAvatar(avatarUrl);
        } else {
            userInfo.setUserId(0);
            userInfo.setNickname("");
            userInfo.setAvatar("");
        }
        return userInfo;
    }
}
