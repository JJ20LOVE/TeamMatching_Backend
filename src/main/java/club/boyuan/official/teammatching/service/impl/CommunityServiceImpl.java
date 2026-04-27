package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.community.CommentDeleteVO;
import club.boyuan.official.teammatching.dto.response.community.CommunityCommentItem;
import club.boyuan.official.teammatching.dto.response.community.CommunityPostDetailItem;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.entity.Comment;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.LikeRecord;
import club.boyuan.official.teammatching.entity.PostImageRelation;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.exception.ResourceNotFoundException;
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
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * 社区服务实现
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final PostImageRelationMapper postImageRelationMapper;
    private final FileResourceMapper fileResourceMapper;

    public CommunityServiceImpl(UserMapper userMapper,
                                CommentMapper commentMapper,
                                LikeRecordMapper likeRecordMapper,
                                PostImageRelationMapper postImageRelationMapper,
                                FileResourceMapper fileResourceMapper) {
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.postImageRelationMapper = postImageRelationMapper;
        this.fileResourceMapper = fileResourceMapper;
    }
    @Override
    @CacheEvict(cacheNames = "communityList", allEntries = true)
    public Number createNewPost(CreatePostRequest request, Integer userId) {
        // 1. 实例化实体类
        CommunityPost post = new CommunityPost();

        BeanUtils.copyProperties(request, post, "images");

        // 3. 填充后端控制的字段
        // postId由后端自动生成
        post.setUserId(userId); // 添加UserId
        // section，title，content，images由前端传入
        post.setStatus(1);
        post.setViewCount(0); // viewCount
        post.setLikeCount(0); // likeCount
        post.setCommentCount(0); // commentCount
        post.setIsTop(false); // isTop
        post.setIsEssence(false); // isEssence
        post.setCreatedTime(LocalDateTime.now()); // createdTime
        post.setUpdateTime(LocalDateTime.now()); // updateTime
        this.save(post);
        savePostImages(post.getPostId(), request.getImages());
        return post.getPostId();
    }

    private void savePostImages(Integer postId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        int order = 0;
        for (String raw : images) {
            Long fileId = resolveImageToFileId(raw);
            if (fileId == null) {
                continue;
            }
            PostImageRelation rel = new PostImageRelation();
            rel.setPostId(postId);
            rel.setFileId(fileId);
            rel.setSortOrder(order++);
            rel.setCreatedTime(LocalDateTime.now());
            try {
                postImageRelationMapper.insert(rel);
            } catch (Exception ignored) {
                // 唯一约束 (post_id, file_id) 等冲突时跳过
            }
        }
    }

    private Long resolveImageToFileId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            FileResource fr = fileResourceMapper.selectOne(
                    new LambdaQueryWrapper<FileResource>().eq(FileResource::getFileUrl, s).last("LIMIT 1"));
            return fr != null ? fr.getFileId() : null;
        }
    }

    @Override
    @Cacheable(cacheNames = "communityList", key = "#request.hashCode() + ':' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<PostListResponse.CommunityPostItem> queryPostList(CommunityQueryRequest request, Integer userId) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();

        // 板块筛选
        if (request.getSection() != null) {
            wrapper.eq(CommunityPost::getSection, request.getSection().getCode());
        }

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, request.getKeyword())
                    .or().like(CommunityPost::getContent, request.getKeyword()));
        }

        // 状态筛选：只查询正常状态
        wrapper.eq(CommunityPost::getStatus, 1);

        // 排序
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

        // 分页查询
        // 使用 Optional 确保即便 request 为空或参数为空，也能有默认值
        long current = Optional.ofNullable(request.getPage()).map(Integer::longValue).orElse(1L);
        long size = Optional.ofNullable(request.getSize()).map(Integer::longValue).orElse(10L);

        Page<CommunityPost> pageParam = new Page<>(current, size);
        Page<CommunityPost> postPage = this.page(pageParam, wrapper);

        List<CommunityPost> posts = postPage.getRecords();
        if (posts == null || posts.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户信息
        List<Integer> userIds = posts.stream().map(CommunityPost::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));

        // 组装响应
        return posts.stream().map(post -> {
            PostListResponse.CommunityPostItem item = new PostListResponse.CommunityPostItem();
            BeanUtils.copyProperties(post, item);

            // 设置用户信息
            User user = userMap.get(post.getUserId());
            PostListResponse.UserInfo userInfo = getUserInfo(user);
            item.setUserInfo(userInfo);

            // 图片列表暂时设为空
            item.setImages(new ArrayList<>());

            return item;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(cacheNames = "communityDetail", key = "#postId"),
        @CacheEvict(cacheNames = "communityComments", allEntries = true)
    })
    public Long createNewComment(Long postId, CreateCommentRequest request, Integer userId) {
        // 1. 基础校验：推荐使用 Assert 语法或自定义工具类，让代码更清爽
        if (postId == null || postId <= 0) throw new BusinessException("帖子ID错误");
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 2. 帖子存在性检查
        CommunityPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException("帖子已不存在");
        }

        // 3. 处理可选参数 parentId (修复外键报错的关键)
        Integer parentId = request.getParentId();
        if (parentId != null && parentId > 0) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || parent.getStatus() == null || parent.getStatus() != 1) {
                throw new BusinessException("回复的评论不存在");
            }
            if (!Objects.equals(parent.getPostId(), post.getPostId())) {
                throw new BusinessException("父评论不属于该帖子");
            }
        } else {
            parentId = null; // 确保是一级评论
        }

        // 4. 构建评论对象
        Comment comment = new Comment();
        comment.setPostId(Math.toIntExact(postId));
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1); // 建议默认 1 (正常)，0 通常留给“删除”或“待审”
        comment.setCreatedTime(LocalDateTime.now());

        // 5. 执行插入
        int insertResult = commentMapper.insert(comment);
        if (insertResult <= 0 || comment.getCommentId() == null) {
            throw new BusinessException("评论发布失败");
        }

        // 6. 更新帖子评论计数 (这里必须持久化！)
        // 进阶技巧：使用 SQL 层面自增（post.setCommentCount(null) + updateWrapper）
        // 或者直接简单地：
        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        this.updateById(post); // 千万别忘了这一步！

        return comment.getCommentId().longValue();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "communityDetail", key = "#request.targetId")
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Cacheable(cacheNames = "communityDetail", key = "#postId", unless = "#result == null")
    public CommunityPostDetailItem getPostDetail(Long postId) {
        if (postId == null || postId <= 0) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        CommunityPost post = this.getById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        int vc = post.getViewCount() == null ? 0 : post.getViewCount();
        post.setViewCount(vc + 1);
        post.setUpdateTime(LocalDateTime.now());
        this.updateById(post);

        CommunityPostDetailItem item = new CommunityPostDetailItem();
        BeanUtils.copyProperties(post, item);
        User author = userMapper.selectById(post.getUserId());
        item.setUserInfo(getUserInfo(author));
        item.setImages(loadPostImageUrls(post.getPostId()));
        return item;
    }

    private List<String> loadPostImageUrls(Integer postId) {
        LambdaQueryWrapper<PostImageRelation> w = new LambdaQueryWrapper<>();
        w.eq(PostImageRelation::getPostId, postId).orderByAsc(PostImageRelation::getSortOrder);
        List<PostImageRelation> rels = postImageRelationMapper.selectList(w);
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> fileIds = rels.stream().map(PostImageRelation::getFileId).filter(Objects::nonNull).toList();
        if (fileIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<FileResource> files = fileResourceMapper.selectBatchIds(fileIds);
        Map<Long, FileResource> byId = files.stream().collect(Collectors.toMap(FileResource::getFileId, f -> f, (a, b) -> a));
        List<String> urls = new ArrayList<>();
        for (PostImageRelation rel : rels) {
            FileResource fr = byId.get(rel.getFileId());
            if (fr != null && fr.getFileUrl() != null && !fr.getFileUrl().isBlank()) {
                urls.add(fr.getFileUrl());
            }
        }
        return urls;
    }

    @Override
    @Cacheable(cacheNames = "communityComments", key = "#postId + ':' + #page + ':' + #size", unless = "#result == null || #result.isEmpty()")
    public List<CommunityCommentItem> getPostComments(Long postId, Integer page, Integer size) {
        if (postId == null || postId <= 0) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        CommunityPost post = this.getById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        long p = Optional.ofNullable(page).map(Integer::longValue).orElse(1L);
        long sz = Optional.ofNullable(size).map(Integer::longValue).orElse(10L);
        if (p < 1) {
            p = 1;
        }
        if (sz < 1) {
            sz = 10;
        }

        LambdaQueryWrapper<Comment> cw = new LambdaQueryWrapper<>();
        cw.eq(Comment::getPostId, postId.intValue()).eq(Comment::getStatus, 1).orderByAsc(Comment::getCreatedTime);
        List<Comment> all = commentMapper.selectList(cw);
        Map<Integer, List<Comment>> byParent = new HashMap<>();
        List<Comment> roots = new ArrayList<>();
        for (Comment c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                byParent.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }
        roots.sort(Comparator.comparing(Comment::getCreatedTime, Comparator.nullsLast(Comparator.naturalOrder())));
        int from = (int) ((p - 1) * sz);
        if (from >= roots.size()) {
            return new ArrayList<>();
        }
        int to = (int) Math.min(from + sz, roots.size());
        List<Comment> pageRoots = roots.subList(from, to);

        Set<Integer> userIds = new HashSet<>();
        for (Comment r : pageRoots) {
            collectSubtreeUserIds(r, byParent, userIds);
        }
        Map<Integer, User> userMap = loadUserMap(userIds);

        List<CommunityCommentItem> out = new ArrayList<>();
        for (Comment r : pageRoots) {
            out.add(toCommentItem(r, byParent, userMap));
        }
        return out;
    }

    private static void collectSubtreeUserIds(Comment c, Map<Integer, List<Comment>> byParent, Set<Integer> out) {
        out.add(c.getUserId());
        for (Comment ch : byParent.getOrDefault(c.getCommentId(), Collections.emptyList())) {
            collectSubtreeUserIds(ch, byParent, out);
        }
    }

    private Map<Integer, User> loadUserMap(Set<Integer> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));
    }

    private CommunityCommentItem toCommentItem(Comment c, Map<Integer, List<Comment>> byParent, Map<Integer, User> userMap) {
        CommunityCommentItem item = new CommunityCommentItem();
        item.setCommentId(c.getCommentId());
        item.setContent(c.getContent());
        item.setLikeCount(c.getLikeCount());
        item.setStatus(c.getStatus());
        item.setCreatedTime(c.getCreatedTime());
        item.setUpdateTime(c.getUpdateTime());
        item.setUserInfo(getUserInfo(userMap.get(c.getUserId())));

        List<Comment> children = new ArrayList<>(byParent.getOrDefault(c.getCommentId(), Collections.emptyList()));
        children.sort(Comparator.comparing(Comment::getCreatedTime, Comparator.nullsLast(Comparator.naturalOrder())));
        for (Comment ch : children) {
            item.getReplies().add(toCommentItem(ch, byParent, userMap));
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "communityComments", allEntries = true)
    public CommentDeleteVO deleteComment(Long commentId, Integer operatorUserId) {
        if (commentId == null || commentId <= 0) {
            throw new ResourceNotFoundException("评论不存在");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == null || comment.getStatus() != 1) {
            throw new ResourceNotFoundException("评论不存在");
        }
        CommunityPost post = this.getById(comment.getPostId().longValue());
        if (post == null) {
            throw new ResourceNotFoundException("帖子不存在");
        }
        User operator = userMapper.selectById(operatorUserId);
        if (operator == null) {
            throw new BusinessException("用户不存在");
        }
        boolean isAdmin = operator.getRole() != null && "admin".equalsIgnoreCase(operator.getRole().trim());
        boolean isPostAuthor = Objects.equals(post.getUserId(), operatorUserId);
        boolean isOwn = Objects.equals(comment.getUserId(), operatorUserId);
        if (!isAdmin && !isPostAuthor && !isOwn) {
            throw new BusinessException("无权限删除该评论");
        }

        comment.setStatus(0);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);

        int cc = post.getCommentCount() == null ? 0 : post.getCommentCount();
        post.setCommentCount(Math.max(0, cc - 1));
        post.setUpdateTime(LocalDateTime.now());
        this.updateById(post);

        return new CommentDeleteVO(true, comment.getCommentId());
    }

    @NonNull
    private static PostListResponse.UserInfo getUserInfo(User user) {
        PostListResponse.UserInfo userInfo = new PostListResponse.UserInfo();
        if (user != null) {
            userInfo.setUserId(user.getUserId());
            userInfo.setNickname(user.getNickname() != null ? user.getNickname() : "");
            userInfo.setAvatar(user.getAvatarFileId() != null ? user.getAvatarFileId().toString() : "");
        } else {
            userInfo.setUserId(0);
            userInfo.setNickname("");
            userInfo.setAvatar("");
        }
        return userInfo;
    }
}
