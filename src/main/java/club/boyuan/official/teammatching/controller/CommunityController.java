package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.response.community.CommentResponse;
import club.boyuan.official.teammatching.dto.response.community.PostDetailResponse;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区相关控制器
 */
@RestController
@RequestMapping("/community")
public class CommunityController {

    // 1. 使用 final 关键字，确保 Service 在初始化后不会被篡改
    private final CommunityService communityService;

    // 2. 构造器注入：Spring 会自动寻找 CommunityService 的实现类并注入
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }
    @PostMapping("/post")
    public PostDetailResponse<PostDetailResponse.PostCreateVO> createPost(@Valid @RequestBody CreatePostRequest request) {
        // 模拟从 JWT 中获取当前登录用户 ID（暂时写死，之后从 SecurityContext 获取）
        Integer currentUserId = 34343333;
        Long newPostId = communityService.createNewPost(request, currentUserId).longValue();
        PostDetailResponse.PostCreateVO vo = new PostDetailResponse.PostCreateVO(newPostId, "发布成功，待审核");
        // 调用 Service 执行发布逻辑
        return PostDetailResponse.success(vo);
    }

    @GetMapping("/posts")
    public PostListResponse getPosts(@Valid CommunityQueryRequest request,
                                      @RequestHeader(value = "Authorization", required = false) String token) {
        Integer currentUserId = 34343333; // 可从 token 解析
        List<PostListResponse.CommunityPostItem> posts = communityService.queryPostList(request, currentUserId);
        return PostListResponse.success(posts);
    }

    @PostMapping("/post/{postId}/comment")
    public CommentResponse CreateComment(@PathVariable Long postId) {

    }
}
