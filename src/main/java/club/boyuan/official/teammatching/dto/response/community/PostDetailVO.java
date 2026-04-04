package club.boyuan.official.teammatching.dto.response.community;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostDetailVO {
    private Integer postId;
    private Integer section;
    private String title;
    private String content;
    private List<String> images;
    private UserInfoDTO userInfo;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean isEssence;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @Data
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Integer userId;
        private String avatar;
        private String nickname;
    }
}
