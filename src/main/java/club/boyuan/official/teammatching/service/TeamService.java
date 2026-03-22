package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.team.JoinConfirmRequest;
import club.boyuan.official.teammatching.dto.response.team.JoinConfirmResponse;

/**
 * 团队服务接口
 */
public interface TeamService {
    JoinConfirmResponse joinConfirm(Integer userId, JoinConfirmRequest request);
}