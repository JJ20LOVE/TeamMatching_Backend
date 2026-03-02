package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.mapper.CommunityPostMapper;
import club.boyuan.official.teammatching.service.CommunityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 社区服务实现
 */
@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {
    // 社区服务实现
    @Override
    public Long createNewPost(CreatePostRequest request, Integer userId) {
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
}