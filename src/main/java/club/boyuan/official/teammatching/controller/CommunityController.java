package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedAuth;
import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.community.CommentCreateVO;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostDetailResponse;
import club.boyuan.official.teammatching.dto.response.community.PostPageResult;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.CommunityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @NeedAuth
    public PostDetailResponse<PostDetailResponse.PostCreateVO> createPost(@Valid @RequestBody CreatePostRequest request) {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        Long newPostId = communityService.createNewPost(request, currentUserId).longValue();
        PostDetailResponse.PostCreateVO vo = new PostDetailResponse.PostCreateVO(newPostId, "发布成功，待审核");
        return PostDetailResponse.success(vo);
    }

    @GetMapping("/posts")
    @NeedLogin
    public PostListResponse getPosts(@Valid CommunityQueryRequest request) {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        PostPageResult result = communityService.queryPostList(request, currentUserId);
        return PostListResponse.success(result);
    }

    @PostMapping("/post/{postId}/comment")
    @NeedLogin
    public CommonResponse<CommentCreateVO> CreateComment(@PathVariable @NotNull(message = "帖子ID不能为空") @Min(1) Long postId,
                                                         @Valid @RequestBody CreateCommentRequest request) {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        Long commentId = communityService.createNewComment(postId, request, currentUserId);
        CommentCreateVO vo = new CommentCreateVO(commentId, "评论成功");
        return CommonResponse.ok(vo);
    }

    @PostMapping("/like")
    @NeedLogin
    public CommonResponse<LikeResponse> Like(@Valid @RequestBody LikeRequest request) {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        LikeResponse response = communityService.toggleLikeStatus(request, currentUserId);
        return CommonResponse.ok(response);
    }
}
