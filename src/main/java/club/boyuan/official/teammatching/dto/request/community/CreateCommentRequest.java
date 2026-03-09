package club.boyuan.official.teammatching.dto.request.community;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建评论请求DTO
 */
@Data
public class CreateCommentRequest {
    @NotBlank
    private String content;

    private Integer parentId;
}
