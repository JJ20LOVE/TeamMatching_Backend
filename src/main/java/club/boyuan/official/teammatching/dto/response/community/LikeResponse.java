package club.boyuan.official.teammatching.dto.response.community;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // 使用 Lombok 简化代码
@AllArgsConstructor
public class LikeResponse {
    private Boolean isLiked;
    private Integer likeCount;
}
