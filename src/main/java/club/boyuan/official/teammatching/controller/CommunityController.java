package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.community.CommentCreateVO;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostDetailResponse;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.service.CommunityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区相关控制器
 */
@RestController
@RequestMapping("/community")
@Validated
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
//      Integer currentUserId = UserContextUtil.getCurrentUserId();
        Integer currentUserId = 1;
        Long newPostId = communityService.createNewPost(request, currentUserId).longValue();
        PostDetailResponse.PostCreateVO vo = new PostDetailResponse.PostCreateVO(newPostId, "发布成功，待审核");
        // 调用 Service 执行发布逻辑
        return PostDetailResponse.success(vo);
    }

    @GetMapping("/posts")
    public PostListResponse getPosts(@Valid CommunityQueryRequest request,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
//      Integer currentUserId = UserContextUtil.getCurrentUserId();
        Integer currentUserId = 1;
        List<PostListResponse.CommunityPostItem> posts = communityService.queryPostList(request, currentUserId);
        return PostListResponse.success(posts);
    }

    @PostMapping("/post/{postId}/comment")
    public CommonResponse<CommentCreateVO> CreateComment(@PathVariable @NotNull(message = "帖子ID不能为空") @Min(1) Long postId,
                                                         @Valid @RequestBody CreateCommentRequest request) {
        //  Integer currentUserId = UserContextUtil.getCurrentUserId();
        Integer currentUserId = 1;
        Long commentId = communityService.createNewComment(postId, request, currentUserId);
        CommentCreateVO vo = new CommentCreateVO(commentId, "评论成功");
        return CommonResponse.ok(vo);
    }

    @PostMapping("/like")
    public CommonResponse<LikeResponse> Like(@Valid @RequestBody LikeRequest request) {
        Integer currentUserId = 1;
//      Integer currentUserId = UserContextUtil.getCurrentUserId();
        LikeResponse response = communityService.toggleLikeStatus(request, currentUserId);
        return CommonResponse.ok(response);
    }
}
