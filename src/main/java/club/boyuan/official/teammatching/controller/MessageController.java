package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedAuth;
import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.chat.SendMessageRequest;
import club.boyuan.official.teammatching.dto.request.chat.UpdateRecruitStatusRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.chat.ChatMessageResponse;
import club.boyuan.official.teammatching.dto.response.chat.ChatSessionResponse;
import club.boyuan.official.teammatching.dto.response.chat.SendMessageResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息相关控制器
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "消息中心")
@lombok.RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    @ApiOperation(value = "获取会话列表", notes = "获取当前用户的所有会话")
    @NeedLogin
    @NeedAuth
    public ResponseEntity<CommonResponse<List<ChatSessionResponse>>> getChatSessions(
            @ApiParam(value = "页码")
            @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "每页数量")
            @RequestParam(value = "size", required = false) Integer size) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        List<ChatSessionResponse> data = messageService.getChatSessions(userId, page, size);
        return ResponseEntity.ok(CommonResponse.ok(data));
    }

    /**
     * 发送消息
     */
    @PostMapping("/message")
    @ApiOperation(value = "发送消息", notes = "在会话中发送消息")
    @NeedLogin
    @NeedAuth
    public ResponseEntity<CommonResponse<SendMessageResponse>> sendMessage(
            @ApiParam(value = "发送消息请求参数", required = true)
            @Valid @RequestBody SendMessageRequest request) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        SendMessageResponse data = messageService.sendMessage(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("发送成功", data));
    }

    /**
     * 获取消息历史
     */
    @GetMapping("/messages")
    @ApiOperation(value = "获取消息历史", notes = "获取会话的历史消息")
    @NeedLogin
    @NeedAuth
    public ResponseEntity<CommonResponse<List<ChatMessageResponse>>> getChatMessages(
            @ApiParam(value = "会话ID", required = true)
            @RequestParam("sessionId") Integer sessionId,
            @ApiParam(value = "查询此时间之前的消息（用于分页）")
            @RequestParam(value = "before", required = false) LocalDateTime before,
            @ApiParam(value = "页码（若不用before）")
            @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "每页数量")
            @RequestParam(value = "size", required = false) Integer size) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        List<ChatMessageResponse> data = messageService.getChatMessages(userId, sessionId, before, page, size);
        return ResponseEntity.ok(CommonResponse.ok(data));
    }

    /**
     * 更新沟通状态
     */
    @PatchMapping("/session/{sessionId}/status")
    @ApiOperation(value = "更新沟通状态", notes = "更新招募沟通状态")
    @NeedLogin
    @NeedAuth
    public ResponseEntity<CommonResponse<Map<String, String>>> updateRecruitStatus(
            @ApiParam(value = "会话ID", required = true)
            @PathVariable Integer sessionId,
            @ApiParam(value = "更新沟通状态请求参数", required = true)
            @Valid @RequestBody UpdateRecruitStatusRequest request) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        Map<String, String> data = messageService.updateRecruitStatus(userId, sessionId, request);
        return ResponseEntity.ok(CommonResponse.ok("更新成功", data));
    }
}