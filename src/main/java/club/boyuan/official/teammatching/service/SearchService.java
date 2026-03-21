package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.search.GlobalSearchRequest;
import club.boyuan.official.teammatching.dto.response.search.GlobalSearchResponse;

/**
 * 全局搜索服务
 */
public interface SearchService {
    GlobalSearchResponse search(GlobalSearchRequest request);
}
