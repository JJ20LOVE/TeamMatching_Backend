package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.entity.Comment;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.entity.LikeRecord;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.CommentMapper;
import club.boyuan.official.teammatching.mapper.CommunityPostMapper;
import club.boyuan.official.teammatching.mapper.LikeRecordMapper;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社区服务实现
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;

    public CommunityServiceImpl(UserMapper userMapper,
                                CommentMapper commentMapper,
                                LikeRecordMapper likeRecordMapper) {
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
    }
    @Override
    public Number createNewPost(CreatePostRequest request, Integer userId) {
        // 1. 实例化实体类
        CommunityPost post = new CommunityPost();

        // 2. 属性拷贝 (将 Request 中的 section, title, content, images 拷贝到 post)
        // 关键：因为我们之前把 Entity 里的 images 改成了 List<String>，这里可以直接拷贝！
        BeanUtils.copyProperties(request, post);

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
        // 4. 调用 MyBatis-Plus 提供的 save 方法
        // 这一步会触发 JacksonTypeHandler，自动把 List<String> 转为数据库里的 JSON 字符串
        this.save(post);
        return post.getPostId();
    }

    @Override
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
            // 进阶：如果传了 parentId，最好查一下父评论是否存在，防止非法 ID 攻击
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null) throw new BusinessException("回复的评论不存在");
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
