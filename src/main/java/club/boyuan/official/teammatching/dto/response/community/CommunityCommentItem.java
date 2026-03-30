package club.boyuan.official.teammatching.dto.response.community;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论项（楼中楼，与 OpenAPI CommunityCommentItem 对齐）
 */
@Data
public class CommunityCommentItem {
    private Integer commentId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private PostListResponse.UserInfo userInfo;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;
    private List<CommunityCommentItem> replies = new ArrayList<>();
}
