package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import club.boyuan.official.teammatching.dto.request.community.CreateCommentRequest;
import club.boyuan.official.teammatching.dto.request.community.CreatePostRequest;
import club.boyuan.official.teammatching.dto.request.community.LikeRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.community.CommentCreateVO;
import club.boyuan.official.teammatching.dto.response.community.CommentDeleteVO;
import club.boyuan.official.teammatching.dto.response.community.CommunityCommentItem;
import club.boyuan.official.teammatching.dto.response.community.CommunityPostDetailItem;
import club.boyuan.official.teammatching.dto.response.community.LikeResponse;
import club.boyuan.official.teammatching.dto.response.community.PostDetailResponse;
import club.boyuan.official.teammatching.dto.response.community.PostListResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.CommunityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区相关控制器
 */
@RestController
@RequestMapping("/community")
@Validated
@Slf4j
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/post")
    public ResponseEntity<CommonResponse<PostDetailResponse.PostCreateVO>> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        log.info("收到发布帖子请求");
        try {
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("用户未登录");
            }
            Long newPostId = communityService.createNewPost(request, currentUserId).longValue();
            PostDetailResponse.PostCreateVO vo =
                    new PostDetailResponse.PostCreateVO(newPostId, "发布成功，待审核");
            return ResponseEntity.ok(CommonResponse.ok(vo));
        } catch (Exception e) {
            log.error("发布帖子失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<CommonResponse<CommunityPostDetailItem>> getPostDetail(
            @PathVariable @NotNull(message = "帖子ID不能为空") @Min(1) Long postId) {
        log.info("收到获取帖子详情请求: postId={}", postId);
        try {
            CommunityPostDetailItem detail = communityService.getPostDetail(postId);
            return ResponseEntity.ok(CommonResponse.ok(detail));
        } catch (Exception e) {
            log.error("获取帖子详情失败: postId={}, error={}", postId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<CommonResponse<List<CommunityCommentItem>>> getPostComments(
            @PathVariable @NotNull(message = "帖子ID不能为空") @Min(1) Long postId,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        log.info("收到获取评论列表请求: postId={}", postId);
        try {
            List<CommunityCommentItem> comments = communityService.getPostComments(postId, page, size);
            return ResponseEntity.ok(CommonResponse.ok(comments));
        } catch (Exception e) {
            log.error("获取评论列表失败: postId={}, error={}", postId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<CommonResponse<CommentDeleteVO>> deleteComment(
            @PathVariable @NotNull(message = "评论ID不能为空") @Min(1) Long commentId) {
        log.info("收到删除评论请求: commentId={}", commentId);
        try {
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("用户未登录");
            }
            CommentDeleteVO vo = communityService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok(CommonResponse.ok(vo));
        } catch (Exception e) {
            log.error("删除评论失败: commentId={}, error={}", commentId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<CommonResponse<List<PostListResponse.CommunityPostItem>>> getPosts(
            @Valid CommunityQueryRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到获取帖子列表请求");
        try {
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            List<PostListResponse.CommunityPostItem> posts =
                    communityService.queryPostList(request, currentUserId);
            return ResponseEntity.ok(CommonResponse.ok(posts));
        } catch (Exception e) {
            log.error("获取帖子列表失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<CommonResponse<CommentCreateVO>> createComment(
            @PathVariable @NotNull(message = "帖子ID不能为空") @Min(1) Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        log.info("收到评论帖子请求: postId={}", postId);
        try {
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("用户未登录");
            }
            Long commentId = communityService.createNewComment(postId, request, currentUserId);
            log.info("用户{}在帖子{}下创建了评论{}", currentUserId, postId, commentId);
            CommentCreateVO vo = new CommentCreateVO(commentId, "评论成功");
            return ResponseEntity.ok(CommonResponse.ok(vo));
        } catch (Exception e) {
            log.error("评论帖子失败: postId={}, error={}", postId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/like")
    public ResponseEntity<CommonResponse<LikeResponse>> like(@Valid @RequestBody LikeRequest request) {
        log.info("收到点赞请求");
        try {
            Integer currentUserId = UserContextUtil.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("用户未登录");
            }
            LikeResponse response = communityService.toggleLikeStatus(request, currentUserId);
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (Exception e) {
            log.error("点赞失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
