package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.talent.CreateTalentCardRequest;
import club.boyuan.official.teammatching.dto.request.talent.TalentInviteRequest;
import club.boyuan.official.teammatching.dto.request.talent.TalentQueryRequest;
import club.boyuan.official.teammatching.dto.request.talent.UpdateTalentStatusRequest;
import club.boyuan.official.teammatching.dto.response.talent.TalentCardResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentDetailResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentInvitationResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentPageResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentSaveResponse;

/**
 * 人才服务接口
 */
public interface TalentService {

    TalentSaveResponse saveOrUpdateCard(Integer currentUserId, CreateTalentCardRequest request);

    TalentDetailResponse getMyCard(Integer currentUserId);

    TalentPageResponse<TalentCardResponse> listTalents(Integer currentUserId, TalentQueryRequest request);

    void updateVisibility(Integer currentUserId, UpdateTalentStatusRequest request);

    TalentInvitationResponse sendInvitation(Integer currentUserId, TalentInviteRequest request);
}
