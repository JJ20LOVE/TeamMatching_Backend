package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.dto.request.search.GlobalSearchRequest;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.search.GlobalSearchResponse;
import club.boyuan.official.teammatching.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局搜索控制器
 */
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 全局搜索
     */
    @GetMapping("/search")
    @NeedLogin
    public ResponseEntity<CommonResponse<GlobalSearchResponse>> search(@Valid GlobalSearchRequest request) {
        log.info("收到全局搜索请求，keyword={}, type={}, sort={}", request.getKeyword(), request.getType(), request.getSort());
        GlobalSearchResponse result = searchService.search(request);
        return ResponseEntity.ok(CommonResponse.ok(result));
    }
}
