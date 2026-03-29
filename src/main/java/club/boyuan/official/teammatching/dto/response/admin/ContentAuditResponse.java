package club.boyuan.official.teammatching.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容审核响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentAuditResponse {

    private List<ContentItemDTO> list;

    /**
     * 内容项DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItemDTO {
        private Integer contentId;
        private String contentType;
        private String title;
        private String content;
        private PublisherDTO publisher;
        private LocalDateTime publishTime;
        private Integer status;
    }

    /**
     * 发布者DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublisherDTO {
        private Integer userId;
        private String username;
        private String nickname;
    }
}
