package club.boyuan.official.teammatching.dto.request.community;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 * 创建帖子请求DTO
 */

@Data
public class CreatePostRequest {
    /**
     * 板块：1/2/3
     */
    @NotNull
    @Min(1)
    @Max(3)
    private Integer section;

    /**
     * 帖子标题
     */
    @NotBlank
    private String title;

    /**
     * 帖子内容
     */
    @NotBlank
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> images;
}