package club.boyuan.official.teammatching.service.impl;

import club.boyuan.official.teammatching.common.enums.ProjectStatusEnum;
import club.boyuan.official.teammatching.dto.request.search.GlobalSearchRequest;
import club.boyuan.official.teammatching.dto.response.search.GlobalSearchResponse;
import club.boyuan.official.teammatching.entity.CommunityPost;
import club.boyuan.official.teammatching.entity.Project;
import club.boyuan.official.teammatching.entity.TalentCard;
import club.boyuan.official.teammatching.mapper.CommunityPostMapper;
import club.boyuan.official.teammatching.mapper.ProjectMapper;
import club.boyuan.official.teammatching.mapper.TalentCardMapper;
import club.boyuan.official.teammatching.service.SearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 全局搜索服务实现
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int RELEVANCE_SCAN_LIMIT = 300;
    private static final String TYPE_ALL = "all";
    private static final String TYPE_PROJECT = "project";
    private static final String TYPE_TALENT = "talent";
    private static final String TYPE_POST = "post";
    private static final String SORT_LATEST = "latest";

    private final ProjectMapper projectMapper;
    private final TalentCardMapper talentCardMapper;
    private final CommunityPostMapper communityPostMapper;

    @Override
    public GlobalSearchResponse search(GlobalSearchRequest request) {
        String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
        String type = normalize(request.getType(), TYPE_ALL);
        String sort = normalize(request.getSort(), "relevance");
        long current = request.getPage() == null ? 1L : request.getPage();
        long size = request.getSize() == null ? 10L : request.getSize();

        GlobalSearchResponse response = new GlobalSearchResponse();
        response.setKeyword(keyword);
        response.setType(type);
        response.setSort(sort);
        response.setPage((int) current);
        response.setSize((int) size);

        if (TYPE_PROJECT.equals(type) || TYPE_ALL.equals(type)) {
            SearchSlice<GlobalSearchResponse.ProjectItem> projectSlice = searchProjects(keyword, sort, current, size);
            response.setProjects(projectSlice.records());
            response.setProjectTotal(projectSlice.total());
        }
        if (TYPE_TALENT.equals(type) || TYPE_ALL.equals(type)) {
            SearchSlice<GlobalSearchResponse.TalentItem> talentSlice = searchTalents(keyword, sort, current, size);
            response.setTalents(talentSlice.records());
            response.setTalentTotal(talentSlice.total());
        }
        if (TYPE_POST.equals(type) || TYPE_ALL.equals(type)) {
            SearchSlice<GlobalSearchResponse.PostItem> postSlice = searchPosts(keyword, sort, current, size);
            response.setPosts(postSlice.records());
            response.setPostTotal(postSlice.total());
        }

        return response;
    }

    private SearchSlice<GlobalSearchResponse.ProjectItem> searchProjects(String keyword, String sort, long current, long size) {
        LambdaQueryWrapper<Project> wrapper = projectSearchWrapper(keyword);
        Long total = projectMapper.selectCount(wrapper);
        if (total == null || total == 0) {
            return new SearchSlice<>(0L, new ArrayList<>());
        }

        List<Project> records;
        if (SORT_LATEST.equals(sort)) {
            wrapper.orderByDesc(Project::getReleaseTime);
            Page<Project> page = projectMapper.selectPage(new Page<>(current, size), wrapper);
            records = page.getRecords();
        } else {
            wrapper.orderByDesc(Project::getReleaseTime);
            Page<Project> page = projectMapper.selectPage(new Page<>(1, RELEVANCE_SCAN_LIMIT), wrapper);
            records = paginateByRelevance(
                    page.getRecords(),
                    current,
                    size,
                    project -> scoreProject(project, keyword),
                    Project::getReleaseTime
            );
        }

        List<GlobalSearchResponse.ProjectItem> items = records.stream().map(project -> {
            GlobalSearchResponse.ProjectItem item = new GlobalSearchResponse.ProjectItem();
            item.setProjectId(project.getProjectId());
            item.setName(project.getName());
            item.setIntro(project.getProjectIntro());
            item.setTrack(project.getBelongTrack());
            item.setStatus(project.getStatus());
            item.setReleaseTime(project.getReleaseTime());
            return item;
        }).collect(Collectors.toList());

        return new SearchSlice<>(total, items);
    }

    private SearchSlice<GlobalSearchResponse.TalentItem> searchTalents(String keyword, String sort, long current, long size) {
        LambdaQueryWrapper<TalentCard> wrapper = talentSearchWrapper(keyword);
        Long total = talentCardMapper.selectCount(wrapper);
        if (total == null || total == 0) {
            return new SearchSlice<>(0L, new ArrayList<>());
        }

        List<TalentCard> records;
        if (SORT_LATEST.equals(sort)) {
            wrapper.orderByDesc(TalentCard::getLastVisibleTime)
                    .orderByDesc(TalentCard::getUpdateTime);
            Page<TalentCard> page = talentCardMapper.selectPage(new Page<>(current, size), wrapper);
            records = page.getRecords();
        } else {
            wrapper.orderByDesc(TalentCard::getLastVisibleTime)
                    .orderByDesc(TalentCard::getUpdateTime);
            Page<TalentCard> page = talentCardMapper.selectPage(new Page<>(1, RELEVANCE_SCAN_LIMIT), wrapper);
            records = paginateByRelevance(
                    page.getRecords(),
                    current,
                    size,
                    talent -> scoreTalent(talent, keyword),
                    talent -> talent.getLastVisibleTime() == null ? talent.getUpdateTime() : talent.getLastVisibleTime()
            );
        }

        List<GlobalSearchResponse.TalentItem> items = records.stream().map(card -> {
            GlobalSearchResponse.TalentItem item = new GlobalSearchResponse.TalentItem();
            item.setCardId(card.getCardId());
            item.setUserId(card.getUserId());
            item.setDisplayName(card.getDisplayName());
            item.setCardTitle(card.getCardTitle());
            item.setTargetDirection(card.getTargetDirection());
            item.setSkillTags(card.getSkillTags());
            item.setLastVisibleTime(card.getLastVisibleTime());
            return item;
        }).collect(Collectors.toList());

        return new SearchSlice<>(total, items);
    }

    private SearchSlice<GlobalSearchResponse.PostItem> searchPosts(String keyword, String sort, long current, long size) {
        LambdaQueryWrapper<CommunityPost> wrapper = postSearchWrapper(keyword);
        Long total = communityPostMapper.selectCount(wrapper);
        if (total == null || total == 0) {
            return new SearchSlice<>(0L, new ArrayList<>());
        }

        List<CommunityPost> records;
        if (SORT_LATEST.equals(sort)) {
            wrapper.orderByDesc(CommunityPost::getCreatedTime);
            Page<CommunityPost> page = communityPostMapper.selectPage(new Page<>(current, size), wrapper);
            records = page.getRecords();
        } else {
            wrapper.orderByDesc(CommunityPost::getCreatedTime);
            Page<CommunityPost> page = communityPostMapper.selectPage(new Page<>(1, RELEVANCE_SCAN_LIMIT), wrapper);
            records = paginateByRelevance(
                    page.getRecords(),
                    current,
                    size,
                    post -> scorePost(post, keyword),
                    CommunityPost::getCreatedTime
            );
        }

        List<GlobalSearchResponse.PostItem> items = records.stream().map(post -> {
            GlobalSearchResponse.PostItem item = new GlobalSearchResponse.PostItem();
            item.setPostId(post.getPostId());
            item.setSection(post.getSection());
            item.setTitle(post.getTitle());
            item.setContent(post.getContent());
            item.setLikeCount(post.getLikeCount());
            item.setCommentCount(post.getCommentCount());
            item.setCreatedTime(post.getCreatedTime());
            return item;
        }).collect(Collectors.toList());

        return new SearchSlice<>(total, items);
    }

    private LambdaQueryWrapper<Project> projectSearchWrapper(String keyword) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getAuditStatus, 1)
                .eq(Project::getStatus, ProjectStatusEnum.RECRUITING.getCode());

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Project::getName, keyword)
                    .or().like(Project::getProjectIntro, keyword)
                    .or().like(Project::getProjectFeatures, keyword)
                    .or().like(Project::getTags, keyword));
        }
        return wrapper;
    }

    private LambdaQueryWrapper<TalentCard> talentSearchWrapper(String keyword) {
        LambdaQueryWrapper<TalentCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalentCard::getStatus, 1)
                .eq(TalentCard::getIsVisible, true);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(TalentCard::getDisplayName, keyword)
                    .or().like(TalentCard::getCardTitle, keyword)
                    .or().like(TalentCard::getTargetDirection, keyword)
                    .or().like(TalentCard::getSkillTags, keyword)
                    .or().like(TalentCard::getSelfStatement, keyword));
        }
        return wrapper;
    }

    private LambdaQueryWrapper<CommunityPost> postSearchWrapper(String keyword) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CommunityPost::getTitle, keyword)
                    .or().like(CommunityPost::getContent, keyword));
        }
        return wrapper;
    }

    private int scoreProject(Project project, String keyword) {
        int score = 0;
        if (containsIgnoreCase(project.getName(), keyword)) {
            score += 3;
        }
        if (containsIgnoreCase(project.getTags(), keyword)) {
            score += 2;
        }
        if (containsIgnoreCase(project.getProjectIntro(), keyword)) {
            score += 1;
        }
        if (containsIgnoreCase(project.getProjectFeatures(), keyword)) {
            score += 1;
        }
        return score;
    }

    private int scoreTalent(TalentCard talent, String keyword) {
        int score = 0;
        if (containsIgnoreCase(talent.getCardTitle(), keyword)) {
            score += 3;
        }
        if (containsIgnoreCase(talent.getTargetDirection(), keyword)) {
            score += 2;
        }
        if (containsIgnoreCase(talent.getSkillTags(), keyword)) {
            score += 2;
        }
        if (containsIgnoreCase(talent.getDisplayName(), keyword)) {
            score += 1;
        }
        if (containsIgnoreCase(talent.getSelfStatement(), keyword)) {
            score += 1;
        }
        return score;
    }

    private int scorePost(CommunityPost post, String keyword) {
        int score = 0;
        if (containsIgnoreCase(post.getTitle(), keyword)) {
            score += 3;
        }
        if (containsIgnoreCase(post.getContent(), keyword)) {
            score += 1;
        }
        if (Boolean.TRUE.equals(post.getIsEssence())) {
            score += 1;
        }
        return score;
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private <T> List<T> paginateByRelevance(List<T> source,
                                            long current,
                                            long size,
                                            Function<T, Integer> scoreFunction,
                                            Function<T, LocalDateTime> timeFunction) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> sorted = source.stream()
                .sorted(Comparator
                        .comparing((T item) -> scoreFunction.apply(item), Comparator.reverseOrder())
                        .thenComparing((T item) -> {
                            LocalDateTime time = timeFunction.apply(item);
                            return time == null ? LocalDateTime.MIN : time;
                        }, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int fromIndex = (int) Math.max(0, (current - 1) * size);
        if (fromIndex >= sorted.size()) {
            return new ArrayList<>();
        }
        int toIndex = (int) Math.min(sorted.size(), fromIndex + size);
        return sorted.subList(fromIndex, toIndex);
    }

    private String normalize(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : defaultValue;
    }

    private record SearchSlice<T>(Long total, List<T> records) {
    }
}
