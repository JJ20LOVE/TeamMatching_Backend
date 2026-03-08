package club.boyuan.official.teammatching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;

import java.util.List;

/**
 * 社区服务接口
 */
public interface CommunityService extends IService<CommunityPost> {
    // 社区服务接口方法
    Number createNewPost(CreatePostRequest request, Integer userId);

    List<PostListResponse.CommunityPostItem> queryPostList(CommunityQueryRequest request, Integer userId);
}