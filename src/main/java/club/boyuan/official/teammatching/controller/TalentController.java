package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.talent.CreateTalentCardRequest;
import club.boyuan.official.teammatching.dto.request.talent.TalentInviteRequest;
import club.boyuan.official.teammatching.dto.request.talent.TalentQueryRequest;
import club.boyuan.official.teammatching.dto.request.talent.UpdateTalentStatusRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentCardResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentDetailResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentInvitationResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentPageResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentSaveResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.TalentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人才相关控制器
 */
@RestController
@RequestMapping("/v1/talent")
@RequiredArgsConstructor
@Validated
@Api(tags = "人才广场")
public class TalentController {

    private final TalentService talentService;

    @PostMapping("/card")
    @NeedLogin
    @ApiOperation(value = "创建/更新人才卡片", notes = "创建或更新当前登录用户的人才卡片")
    public ResponseEntity<CommonResponse<TalentSaveResponse>> saveOrUpdateCard(
            @Valid @RequestBody CreateTalentCardRequest request) {
        Integer currentUserId = requireCurrentUserId();
        TalentSaveResponse response = talentService.saveOrUpdateCard(currentUserId, request);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @GetMapping("/card/my")
    @NeedLogin
    @ApiOperation(value = "获取我的人才卡片", notes = "获取当前登录用户的人才卡片详情")
    public ResponseEntity<CommonResponse<TalentDetailResponse>> getMyCard() {
        Integer currentUserId = requireCurrentUserId();
        TalentDetailResponse response = talentService.getMyCard(currentUserId);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @GetMapping("/list")
    @NeedLogin
    @ApiOperation(value = "人才卡片列表", notes = "人才广场分页列表")
    public ResponseEntity<CommonResponse<TalentPageResponse<TalentCardResponse>>> listTalents(
            @Valid TalentQueryRequest request) {
        Integer currentUserId = requireCurrentUserId();
        TalentPageResponse<TalentCardResponse> response = talentService.listTalents(currentUserId, request);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @PatchMapping("/card/visibility")
    @NeedLogin
    @ApiOperation(value = "切换人才卡可见性", notes = "修改当前用户人才卡的可见状态")
    public ResponseEntity<CommonResponse<Void>> updateVisibility(
            @Valid @RequestBody UpdateTalentStatusRequest request) {
        Integer currentUserId = requireCurrentUserId();
        talentService.updateVisibility(currentUserId, request);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @PostMapping("/invite")
    @NeedLogin
    @ApiOperation(value = "发送邀请", notes = "项目发布者向人才卡发送组队邀请")
    public ResponseEntity<CommonResponse<TalentInvitationResponse>> sendInvitation(
            @Valid @RequestBody TalentInviteRequest request) {
        Integer currentUserId = requireCurrentUserId();
        TalentInvitationResponse response = talentService.sendInvitation(currentUserId, request);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    private Integer requireCurrentUserId() {
        Integer currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        return currentUserId;
    }
}
