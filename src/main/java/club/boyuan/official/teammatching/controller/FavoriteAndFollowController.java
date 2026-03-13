package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 收藏与关注相关接口
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Api(tags = "收藏与关注")
public class FavoriteAndFollowController {

    private final ProjectService projectService;

    /**
     * 收藏/取消收藏项目（切换）
     */
    @PostMapping("/favorite/project/{projectId}")
    @ApiOperation(value = "收藏/取消收藏项目", notes = "对应功能 F-09，收藏或取消收藏项目（切换操作）")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, Boolean>>> toggleFavoriteProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable Integer projectId) {

        log.info("收到项目收藏切换请求，projectId: {}", projectId);

        try {
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            boolean isFavored = projectService.toggleFavoriteProject(projectId, userId);

            Map<String, Boolean> data = new HashMap<>();
            data.put("isFavored", isFavored);

            return ResponseEntity.ok(CommonResponse.ok("操作成功", data));
        } catch (Exception e) {
            log.error("项目收藏切换失败：projectId={}, error={}", projectId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 关注/取消关注发布者（切换）
     */
    @PostMapping("/follow/{userId}")
    @ApiOperation(value = "关注/取消关注用户", notes = "对应功能 F-09，关注或取消关注用户（切换操作）")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, Boolean>>> toggleFollowUser(
            @ApiParam(value = "被关注者 ID", required = true)
            @PathVariable("userId") Integer targetUserId) {

        log.info("收到用户关注切换请求，targetUserId: {}", targetUserId);

        try {
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            boolean isFollowed = projectService.toggleFollowUser(targetUserId, userId);

            Map<String, Boolean> data = new HashMap<>();
            data.put("isFollowed", isFollowed);

            return ResponseEntity.ok(CommonResponse.ok("操作成功", data));
        } catch (Exception e) {
            log.error("用户关注切换失败：targetUserId={}, error={}", targetUserId, e.getMessage(), e);
            throw e;
        }
    }
}

