package club.boyuan.official.teammatching.dto.response.search;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局搜索响应
 */
@Data
public class GlobalSearchResponse {
    private String keyword;
    private String type;
    private String sort;
    private Integer page;
    private Integer size;

    private Long projectTotal = 0L;
    private Long talentTotal = 0L;
    private Long postTotal = 0L;

    private List<ProjectItem> projects = new ArrayList<>();
    private List<TalentItem> talents = new ArrayList<>();
    private List<PostItem> posts = new ArrayList<>();

    @Data
    public static class ProjectItem {
        private Integer projectId;
        private String name;
        private String intro;
        private String track;
        private Integer status;
        private LocalDateTime releaseTime;
    }

    @Data
    public static class TalentItem {
        private Integer cardId;
        private Integer userId;
        private String displayName;
        private String cardTitle;
        private String targetDirection;
        private String skillTags;
        private LocalDateTime lastVisibleTime;
    }

    @Data
    public static class PostItem {
        private Integer postId;
        private Integer section;
        private String title;
        private String content;
        private Integer likeCount;
        private Integer commentCount;
        private LocalDateTime createdTime;
    }
}
