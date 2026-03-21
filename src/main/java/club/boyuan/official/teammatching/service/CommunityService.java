package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostPageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;

/**
 * 社区服务接口
 */
public interface CommunityService extends IService<CommunityPost> {
    // 社区服务接口方法
    Number createNewPost(CreatePostRequest request, Integer userId);

    PostPageResult queryPostList(CommunityQueryRequest request, Integer userId);

    Long createNewComment(Long postId, CreateCommentRequest request, Integer userId);

    LikeResponse toggleLikeStatus(LikeRequest request, Integer userId);
}
