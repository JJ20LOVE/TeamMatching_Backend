package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRequest;
import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRespondRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.contact.ContactExchangeResponse;
import club.boyuan.official.teammatching.dto.response.contact.ContactInfoResponse;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.service.ContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Api(tags = "联系方式交换")
public class ContactController {

    private final ContactService contactService;

    /**
     * 请求交换联系方式
     */
    @PostMapping("/contact/exchange/request")
    @ApiOperation(value = "请求交换联系方式", notes = "发起交换联系方式请求")
    @NeedLogin
    public ResponseEntity<CommonResponse<ContactExchangeResponse>> requestExchange(
            @ApiParam(value = "请求参数", required = true)
            @Valid @RequestBody ContactExchangeRequest request) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        ContactExchangeResponse data = contactService.requestExchange(userId, request);
        return ResponseEntity.ok(CommonResponse.ok("success", data));
    }

    /**
     * 处理交换请求
     */
    @PostMapping("/contact/exchange/{exchangeId}/respond")
    @ApiOperation(value = "处理交换请求", notes = "同意或拒绝联系方式交换请求")
    @NeedLogin
    public ResponseEntity<CommonResponse<Map<String, Object>>> respondExchange(
            @ApiParam(value = "交换记录ID", required = true)
            @PathVariable Integer exchangeId,
            @ApiParam(value = "处理参数", required = true)
            @Valid @RequestBody ContactExchangeRespondRequest request) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        Map<String, Object> data = contactService.respondExchange(userId, exchangeId, request);
        return ResponseEntity.ok(CommonResponse.ok("success", data));
    }

    /**
     * 获取联系方式
     */
    @GetMapping("/contact/{userId}")
    @ApiOperation(value = "获取联系方式", notes = "交换成功后获取对方的联系方式")
    @NeedLogin
    public ResponseEntity<CommonResponse<ContactInfoResponse>> getContactInfo(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable("userId") Integer targetUserId) {

        Integer userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        ContactInfoResponse data = contactService.getContactInfo(userId, targetUserId);
        return ResponseEntity.ok(CommonResponse.ok("success", data));
    }
}

