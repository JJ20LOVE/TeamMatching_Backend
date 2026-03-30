package club.boyuan.official.teammatching.dto.response.community;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子详情（与 OpenAPI CommunityPostDetailItem 对齐）
 */
@Data
public class CommunityPostDetailItem {
    private Integer postId;
    private Integer section;
    private String title;
    private String content;
    private List<String> images = new ArrayList<>();
    private PostListResponse.UserInfo userInfo;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean isEssence;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;
}
