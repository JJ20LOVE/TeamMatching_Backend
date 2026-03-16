package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.team.JoinConfirmRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.team.JoinConfirmResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 团队相关控制器
 */
@RestController
@RequestMapping("/team")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "团队")
public class TeamController {

    private final TeamService teamService;

    /**
     * 确认入队
     */
    @PostMapping("/join-confirm")
    @ApiOperation(value = "确认入队", notes = "学生确认加入团队")
    @NeedLogin
    public ResponseEntity<CommonResponse<JoinConfirmResponse>> joinConfirm(
            @ApiParam(value = "确认入队请求参数", required = true)
            @Valid @RequestBody JoinConfirmRequest request) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        JoinConfirmResponse data = teamService.joinConfirm(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("success", data));
    }
}