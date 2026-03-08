package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.mapper.CommunityPostMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.service.CommunityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区服务实现
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    private final UserMapper userMapper;

    public CommunityServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        post.setStatus(0); // 0表示待审核
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
        Page<CommunityPost> page = new Page<>(request.getPage(), request.getSize());
        Page<CommunityPost> postPage = this.page(page, wrapper);

        List<CommunityPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
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