package club.boyuan.official.teammatching.dto.response.community;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子分页结果
 */
@Data
public class PostPageResult {
    private List<PostListResponse.CommunityPostItem> records = new ArrayList<>();
    private Long total = 0L;
    private Long current = 1L;
    private Long size = 10L;
    private Long pages = 0L;
}
