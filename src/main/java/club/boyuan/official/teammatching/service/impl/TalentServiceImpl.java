package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.constants.TalentSortConstants;
import club.boyuan.official.teammatching.dto.request.talent.CreateTalentCardRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import club.boyuan.official.teammatching.dto.request.talent.TalentInviteRequest;
import club.boyuan.official.teammatching.dto.request.talent.TalentQueryRequest;
import club.boyuan.official.teammatching.dto.request.talent.UpdateTalentStatusRequest;
import club.boyuan.official.teammatching.dto.response.talent.TalentCardResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentDetailResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentInvitationListItemResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentInvitationResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentPageResponse;
import club.boyuan.official.teammatching.dto.response.talent.TalentSaveResponse;
import club.boyuan.official.teammatching.entity.FileResource;
import club.boyuan.official.teammatching.entity.Follow;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.TalentCard;
import club.boyuan.official.teammatching.entity.TalentInvitation;
import club.boyuan.official.teammatching.entity.User;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.FileResourceMapper;
import club.boyuan.official.teammatching.mapper.FollowMapper;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.TalentCardMapper;
import club.boyuan.official.teammatching.mapper.TalentInvitationMapper;
import club.boyuan.official.teammatching.mapper.UserMapper;
import club.boyuan.official.teammatching.mq.producer.NotificationProducer;
import club.boyuan.official.teammatching.mq.support.NotificationPreferenceUtils;
import club.boyuan.official.teammatching.service.TalentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalentServiceImpl implements TalentService {

    private static final Integer USER_AUTH_PASSED = 1;
    private static final Integer CARD_STATUS_OPEN = 1;
    private static final Integer INVITATION_STATUS_PENDING = 0;

    private final TalentCardMapper talentCardMapper;
    private final TalentInvitationMapper talentInvitationMapper;
    private final NotificationProducer notificationProducer;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final FileResourceMapper fileResourceMapper;
    private final FollowMapper followMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "talentDetail", allEntries = true)
    public TalentSaveResponse saveOrUpdateCard(Integer currentUserId, CreateTalentCardRequest request) {
        User currentUser = getUserOrThrow(currentUserId);
        if (!Objects.equals(currentUser.getAuthStatus(), USER_AUTH_PASSED)) {
            throw new BusinessException("用户未通过认证，暂不能发布人才卡");
        }

        TalentCard existedCard = getCardByUserId(currentUserId);
        Long resumeFileId = resolveFileIdByUrl(request.getResumeUrl());
        Long portfolioFileId = resolveFileIdByUrl(request.getPortfolioUrl());

        LocalDateTime now = LocalDateTime.now();
        boolean isCreate = existedCard == null;
        TalentCard card = isCreate ? new TalentCard() : existedCard;

        if (isCreate) {
            card.setUserId(currentUserId);
            card.setCreatedTime(now);
            card.setViewCount(0);
            card.setInviteCount(0);
        }

        card.setStatus(request.getStatus());
        card.setDisplayName(resolveDisplayName(request.getDisplayName(), currentUser));
        card.setMajor(currentUser.getMajor());
        card.setGrade(currentUser.getGrade());
        card.setCardTitle(request.getCardTitle());
        card.setTargetDirection(request.getTargetDirection());
        card.setExpectedCompetition(request.getExpectedCompetition());
        card.setExpectedRole(request.getExpectedRole());
        card.setSelfStatement(request.getSelfStatement());
        card.setSkillTags(request.getSkillTags());
        card.setResumeFileId(resumeFileId);
        card.setPortfolioFileId(portfolioFileId);
        card.setGithubUrl(request.getGithubUrl());

        boolean visible = Objects.equals(request.getStatus(), CARD_STATUS_OPEN);
        card.setIsVisible(visible);
        if (visible) {
            card.setLastVisibleTime(now);
        }
        card.setUpdateTime(now);

        if (isCreate) {
            talentCardMapper.insert(card);
        } else {
            talentCardMapper.updateById(card);
        }

        LambdaUpdateWrapper<User> userUpdate = new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, currentUserId)
                .set(User::getTalentCardId, card.getCardId())
                .set(User::getIsTalentVisible, card.getIsVisible())
                .set(User::getUpdateTime, now);
        userMapper.update(null, userUpdate);

        TalentSaveResponse response = new TalentSaveResponse();
        response.setCardId(card.getCardId());
        response.setStatus(card.getStatus());
        response.setCreateTime(card.getCreatedTime());
        return response;
    }

    @Override
    @Cacheable(cacheNames = "talentDetail", key = "'my:' + #currentUserId", unless = "#result == null")
    public TalentDetailResponse getMyCard(Integer currentUserId) {
        TalentCard card = getCardByUserId(currentUserId);
        if (card == null) {
            throw new BusinessException("人才卡不存在，请先创建");
        }
        return toTalentDetailResponse(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Cacheable(cacheNames = "talentDetail", key = "#currentUserId + ':' + #cardId", unless = "#result == null")
    public TalentDetailResponse getCardDetail(Integer currentUserId, Integer cardId) {
        TalentCard card = talentCardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("人才卡不存在");
        }
        boolean isOwner = Objects.equals(card.getUserId(), currentUserId);
        if (!isOwner && (!Boolean.TRUE.equals(card.getIsVisible()) || !Objects.equals(card.getStatus(), CARD_STATUS_OPEN))) {
            throw new BusinessException("人才卡不可见");
        }
        if (!isOwner) {
            LocalDateTime now = LocalDateTime.now();
            card.setViewCount((card.getViewCount() == null ? 0 : card.getViewCount()) + 1);
            card.setUpdateTime(now);
            talentCardMapper.updateById(card);
        }
        return toTalentDetailResponse(card);
    }

    @Override
    @Cacheable(cacheNames = "talentList", key = "#currentUserId + ':' + #request.hashCode()", unless = "#result == null || #result.records == null || #result.records.isEmpty()")
    public TalentPageResponse<TalentCardResponse> listTalents(Integer currentUserId, TalentQueryRequest request) {
        long current = resolveCurrentPage(request);
        long size = resolvePageSize(request);

        LambdaQueryWrapper<TalentCard> wrapper = new LambdaQueryWrapper<TalentCard>()
                .eq(TalentCard::getStatus, CARD_STATUS_OPEN)
                .eq(TalentCard::getIsVisible, true);

        if (StringUtils.hasText(request.getMajor())) {
            wrapper.eq(TalentCard::getMajor, request.getMajor().trim());
        }
        if (StringUtils.hasText(request.getGrade())) {
            wrapper.eq(TalentCard::getGrade, request.getGrade().trim());
        }
        if (StringUtils.hasText(request.getSkillTags())) {
            wrapper.like(TalentCard::getSkillTags, request.getSkillTags().trim());
        }
        if (StringUtils.hasText(request.getExpectedCompetition())) {
            wrapper.like(TalentCard::getExpectedCompetition, request.getExpectedCompetition().trim());
        }

        String sort = normalizeSort(request.getSort());
        applySort(wrapper, sort);

        Page<TalentCard> page = new Page<>(current, size);
        Page<TalentCard> resultPage = talentCardMapper.selectPage(page, wrapper);
        List<TalentCard> cards = resultPage.getRecords();

        if (cards == null || cards.isEmpty()) {
            return TalentPageResponse.of(Collections.emptyList(), resultPage.getTotal(), resultPage.getSize(),
                    resultPage.getCurrent(), resultPage.getPages());
        }

        Set<Integer> followedUserIds = queryFollowedUserIds(currentUserId, cards);
        List<TalentCardResponse> records = cards.stream().map(card -> {
            TalentCardResponse item = new TalentCardResponse();
            item.setCardId(card.getCardId());
            item.setUserId(card.getUserId());
            item.setDisplayName(card.getDisplayName());
            item.setMajor(card.getMajor());
            item.setGrade(card.getGrade());
            item.setCardTitle(card.getCardTitle());
            item.setTargetDirection(card.getTargetDirection());
            item.setExpectedCompetition(card.getExpectedCompetition());
            item.setSkillTags(card.getSkillTags());
            item.setViewCount(card.getViewCount());
            item.setInviteCount(card.getInviteCount());
            item.setCreatedTime(card.getCreatedTime());
            item.setIsFollowing(followedUserIds.contains(card.getUserId()));
            return item;
        }).toList();

        return TalentPageResponse.of(records, resultPage.getTotal(), resultPage.getSize(),
                resultPage.getCurrent(), resultPage.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "talentDetail", allEntries = true)
    public void updateVisibility(Integer currentUserId, UpdateTalentStatusRequest request) {
        TalentCard card = getCardByUserId(currentUserId);
        if (card == null) {
            throw new BusinessException("人才卡不存在，请先创建");
        }

        LocalDateTime now = LocalDateTime.now();
        card.setIsVisible(request.getIsVisible());
        if (Boolean.TRUE.equals(request.getIsVisible())) {
            card.setLastVisibleTime(now);
        }
        card.setUpdateTime(now);
        talentCardMapper.updateById(card);

        LambdaUpdateWrapper<User> userUpdate = new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, currentUserId)
                .set(User::getIsTalentVisible, request.getIsVisible())
                .set(User::getUpdateTime, now);
        userMapper.update(null, userUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TalentInvitationResponse sendInvitation(Integer currentUserId, TalentInviteRequest request) {
        User captainUser = getUserOrThrow(currentUserId);
        if (StringUtils.hasText(request.getContactInfo())) {
            captainUser.setContactInfo(request.getContactInfo().trim());
            captainUser.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(captainUser);
        }

        TalentCard talentCard = talentCardMapper.selectById(request.getTalentCardId());
        if (talentCard == null) {
            throw new BusinessException("人才卡不存在");
        }
        if (Objects.equals(talentCard.getUserId(), currentUserId)) {
            throw new BusinessException("不能邀请自己");
        }

        List<InvitationProjectContext> inviteProjects = resolveInviteProjects(currentUserId, request);

        LocalDateTime now = LocalDateTime.now();
        List<TalentInvitation> createdInvitations = new ArrayList<>();
        for (InvitationProjectContext ctx : inviteProjects) {
            LambdaQueryWrapper<TalentInvitation> duplicatedWrapper = new LambdaQueryWrapper<TalentInvitation>()
                    .eq(TalentInvitation::getCaptainId, currentUserId)
                    .eq(TalentInvitation::getTalentCardId, request.getTalentCardId())
                    .eq(TalentInvitation::getStatus, INVITATION_STATUS_PENDING);
            if (ctx.projectId != null) {
                duplicatedWrapper.eq(TalentInvitation::getProjectId, ctx.projectId);
            } else {
                duplicatedWrapper.isNull(TalentInvitation::getProjectId);
                if (StringUtils.hasText(ctx.projectName)) {
                    duplicatedWrapper.eq(TalentInvitation::getProjectName, ctx.projectName);
                } else {
                    duplicatedWrapper.isNull(TalentInvitation::getProjectName);
                }
            }
            Long duplicated = talentInvitationMapper.selectCount(duplicatedWrapper);
            if (duplicated != null && duplicated > 0) {
                throw new BusinessException("该人才卡已存在待处理邀请，请勿重复发送");
            }

            TalentInvitation invitation = new TalentInvitation();
            invitation.setCaptainId(currentUserId);
            invitation.setTalentId(talentCard.getUserId());
            invitation.setProjectId(ctx.projectId);
            invitation.setTalentCardId(request.getTalentCardId());
            invitation.setInvitationMessage(request.getInvitationMessage());
            invitation.setProjectName(ctx.projectName);
            invitation.setProjectRole(request.getProjectRole());
            invitation.setStatus(INVITATION_STATUS_PENDING);
            invitation.setReadStatus(false);
            invitation.setSendTime(now);
            talentInvitationMapper.insert(invitation);
            createdInvitations.add(invitation);
        }

        User talentUser = userMapper.selectById(talentCard.getUserId());
        if (talentUser != null && NotificationPreferenceUtils.isChannelEnabled(talentUser.getInvitationNotify())) {
            User captain = userMapper.selectById(currentUserId);
            String capName = captain != null && StringUtils.hasText(captain.getNickname()) ? captain.getNickname() : "队长";
            String displayProjectName = StringUtils.hasText(createdInvitations.get(0).getProjectName())
                    ? createdInvitations.get(0).getProjectName() : "未命名项目";
            notificationProducer.publishInvitation(
                    talentCard.getUserId(),
                    "组队邀请",
                    capName + " 邀请你加入项目「" + displayProjectName + "」",
                    "talent_invitation",
                    String.valueOf(createdInvitations.get(0).getInvitationId()));
        }

        LambdaUpdateWrapper<TalentCard> cardUpdate = new LambdaUpdateWrapper<TalentCard>()
                .eq(TalentCard::getCardId, talentCard.getCardId())
                .setSql("invite_count = IFNULL(invite_count, 0) + " + createdInvitations.size())
                .set(TalentCard::getUpdateTime, now);
        talentCardMapper.update(null, cardUpdate);

        TalentInvitation invitation = createdInvitations.get(0);
        TalentInvitationResponse response = new TalentInvitationResponse();
        response.setInvitationId(invitation.getInvitationId());
        response.setCaptainId(invitation.getCaptainId());
        response.setTalentId(invitation.getTalentId());
        response.setProjectId(invitation.getProjectId());
        response.setTalentCardId(invitation.getTalentCardId());
        response.setProjectName(invitation.getProjectName());
        response.setProjectRole(invitation.getProjectRole());
        response.setInvitationMessage(invitation.getInvitationMessage());
        response.setContactInfo(captainUser.getContactInfo());
        response.setStatus(invitation.getStatus());
        response.setSendTime(invitation.getSendTime());
        return response;
    }

    private List<InvitationProjectContext> resolveInviteProjects(Integer currentUserId, TalentInviteRequest request) {
        boolean hasManualInput = Boolean.TRUE.equals(request.getHasManualInput());
        if (hasManualInput) {
            if (!StringUtils.hasText(request.getCustomProjectName())) {
                throw new BusinessException("hasManualInput=true 时 customProjectName 不能为空");
            }
            return List.of(new InvitationProjectContext(null, request.getCustomProjectName().trim()));
        }

        Set<Integer> projectIds = new java.util.LinkedHashSet<>();
        if (request.getProjectId() != null) {
            projectIds.add(request.getProjectId());
        }
        if (request.getProjectIds() != null) {
            for (Integer pid : request.getProjectIds()) {
                if (pid != null) {
                    projectIds.add(pid);
                }
            }
        }
        if (projectIds.isEmpty()) {
            // 允许不选项目也不手动输入：发送“泛邀请”，项目字段为空
            return List.of(new InvitationProjectContext(null, null));
        }

        List<Project> projects = projectMapper.selectBatchIds(projectIds);
        if (projects == null || projects.size() != projectIds.size()) {
            throw new BusinessException("存在无效项目ID");
        }
        List<InvitationProjectContext> contexts = new ArrayList<>();
        for (Project p : projects) {
            if (!Objects.equals(p.getPublisherUserId(), currentUserId)) {
                throw new BusinessException("包含非当前用户发布的项目，不能发送邀请");
            }
            contexts.add(new InvitationProjectContext(p.getProjectId(), p.getName()));
        }
        return contexts;
    }

    private record InvitationProjectContext(Integer projectId, String projectName) { }

    @Override
    public List<TalentInvitationListItemResponse> listSentInvitations(Integer currentUserId, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page;
        long pageSize = size == null || size < 1 ? 10L : size;

        LambdaQueryWrapper<TalentInvitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalentInvitation::getCaptainId, currentUserId)
                .orderByDesc(TalentInvitation::getSendTime, TalentInvitation::getInvitationId);
        Page<TalentInvitation> pageParam = new Page<>(current, pageSize);
        List<TalentInvitation> records = talentInvitationMapper.selectPage(pageParam, wrapper).getRecords();
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return buildInvitationItems(records, false);
    }

    @Override
    public List<TalentInvitationListItemResponse> listReceivedInvitations(Integer currentUserId, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page;
        long pageSize = size == null || size < 1 ? 10L : size;

        LambdaQueryWrapper<TalentInvitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalentInvitation::getTalentId, currentUserId)
                .orderByDesc(TalentInvitation::getSendTime, TalentInvitation::getInvitationId);
        Page<TalentInvitation> pageParam = new Page<>(current, pageSize);
        List<TalentInvitation> records = talentInvitationMapper.selectPage(pageParam, wrapper).getRecords();
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return buildInvitationItems(records, true);
    }

    private List<TalentInvitationListItemResponse> buildInvitationItems(List<TalentInvitation> records, boolean receivedView) {
        Set<Integer> userIdsForLookup = records.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getCaptainId(), receivedView ? item.getCaptainId() : item.getTalentId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = new HashMap<>();
        if (!userIdsForLookup.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIdsForLookup);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));
        }

        List<TalentInvitationListItemResponse> out = new ArrayList<>();
        for (TalentInvitation inv : records) {
            TalentInvitationListItemResponse row = new TalentInvitationListItemResponse();
            row.setInvitationId(inv.getInvitationId());
            row.setProjectId(inv.getProjectId());
            row.setProjectName(inv.getProjectName());
            row.setProjectRole(inv.getProjectRole());
            row.setTalentCardId(inv.getTalentCardId());
            row.setInvitationMessage(inv.getInvitationMessage());
            row.setStatus(inv.getStatus());
            row.setReadStatus(inv.getReadStatus());
            row.setSendTime(inv.getSendTime());
            row.setResponseTime(inv.getResponseTime());
            Integer counterpartUserId = receivedView ? inv.getCaptainId() : inv.getTalentId();
            row.setCounterpartUserId(counterpartUserId);

            User counterpart = userMap.get(counterpartUserId);
            if (counterpart != null) {
                row.setCounterpartNickname(counterpart.getNickname());
                row.setCounterpartAvatar(counterpart.getAvatarFileId() != null ? counterpart.getAvatarFileId().toString() : null);
            }
            User captain = userMap.get(inv.getCaptainId());
            if (captain != null) {
                row.setContactInfo(captain.getContactInfo());
            }
            out.add(row);
        }
        return out;
    }

    private User getUserOrThrow(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private TalentCard getCardByUserId(Integer userId) {
        return talentCardMapper.selectOne(
                new LambdaQueryWrapper<TalentCard>()
                        .eq(TalentCard::getUserId, userId)
                        .last("limit 1")
        );
    }

    private Long resolveFileIdByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        FileResource fileResource = fileResourceMapper.selectOne(
                new LambdaQueryWrapper<FileResource>()
                        .eq(FileResource::getFileUrl, fileUrl.trim())
                        .eq(FileResource::getIsDeleted, false)
                        .last("limit 1")
        );
        if (fileResource == null) {
            throw new BusinessException("未找到对应文件资源，请先上传文件后再提交URL");
        }
        return fileResource.getFileId();
    }

    private String resolveFileUrlById(Long fileId) {
        if (fileId == null) {
            return null;
        }
        FileResource fileResource = fileResourceMapper.selectById(fileId);
        if (fileResource == null || Boolean.TRUE.equals(fileResource.getIsDeleted())) {
            return null;
        }
        return fileResource.getFileUrl();
    }

    private Set<Integer> queryFollowedUserIds(Integer currentUserId, List<TalentCard> cards) {
        if (currentUserId == null) {
            return Collections.emptySet();
        }
        List<Integer> talentUserIds = cards.stream()
                .map(TalentCard::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (talentUserIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, currentUserId)
                        .in(Follow::getFollowingId, talentUserIds)
        );
        if (follows == null || follows.isEmpty()) {
            return Collections.emptySet();
        }
        return follows.stream().map(Follow::getFollowingId).collect(Collectors.toSet());
    }

    private String resolveDisplayName(String requestDisplayName, User currentUser) {
        if (StringUtils.hasText(requestDisplayName)) {
            return requestDisplayName.trim();
        }
        if (StringUtils.hasText(currentUser.getNickname())) {
            return currentUser.getNickname();
        }
        return currentUser.getUsername();
    }

    private TalentDetailResponse toTalentDetailResponse(TalentCard card) {
        TalentDetailResponse response = new TalentDetailResponse();
        response.setCardId(card.getCardId());
        response.setUserId(card.getUserId());
        response.setStatus(card.getStatus());
        response.setIsVisible(card.getIsVisible());
        response.setDisplayName(card.getDisplayName());
        response.setMajor(card.getMajor());
        response.setGrade(card.getGrade());
        response.setCardTitle(card.getCardTitle());
        response.setTargetDirection(card.getTargetDirection());
        response.setExpectedCompetition(card.getExpectedCompetition());
        response.setExpectedRole(card.getExpectedRole());
        response.setSelfStatement(card.getSelfStatement());
        response.setSkillTags(card.getSkillTags());
        response.setResumeUrl(resolveFileUrlById(card.getResumeFileId()));
        response.setPortfolioUrl(resolveFileUrlById(card.getPortfolioFileId()));
        response.setGithubUrl(card.getGithubUrl());
        response.setViewCount(card.getViewCount());
        response.setInviteCount(card.getInviteCount());
        response.setCreatedTime(card.getCreatedTime());
        return response;
    }

    private String normalizeSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return TalentSortConstants.LATEST;
        }
        String value = sort.trim().toLowerCase();
        if (TalentSortConstants.HOT.equals(value)
                || TalentSortConstants.RECOMMEND.equals(value)
                || TalentSortConstants.LATEST.equals(value)) {
            return value;
        }
        return TalentSortConstants.LATEST;
    }

    private void applySort(LambdaQueryWrapper<TalentCard> wrapper, String sort) {
        if (TalentSortConstants.HOT.equals(sort)) {
            wrapper.orderByDesc(TalentCard::getViewCount)
                    .orderByDesc(TalentCard::getInviteCount)
                    .orderByDesc(TalentCard::getUpdateTime)
                    .orderByDesc(TalentCard::getCardId);
            return;
        }
        if (TalentSortConstants.RECOMMEND.equals(sort)) {
            wrapper.orderByDesc(TalentCard::getInviteCount)
                    .orderByDesc(TalentCard::getViewCount)
                    .orderByDesc(TalentCard::getLastVisibleTime)
                    .orderByDesc(TalentCard::getCardId);
            return;
        }
        wrapper.orderByDesc(TalentCard::getLastVisibleTime)
                .orderByDesc(TalentCard::getUpdateTime)
                .orderByDesc(TalentCard::getCardId);
    }

    private long resolveCurrentPage(TalentQueryRequest request) {
        Integer current = request.getCurrent() != null ? request.getCurrent() : request.getPage();
        if (current == null || current < 1) {
            return 1L;
        }
        return current.longValue();
    }

    private long resolvePageSize(TalentQueryRequest request) {
        Integer size = request.getSize();
        if (size == null || size < 1) {
            return 20L;
        }
        return Math.min(size.longValue(), 50L);
    }
}
