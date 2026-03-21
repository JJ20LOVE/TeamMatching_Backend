package club.boyuan.official.teammatching.dto.request.search;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 全局搜索请求
 */
@Data
public class GlobalSearchRequest {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Pattern(regexp = "all|project|talent|post", message = "搜索范围仅支持 all/project/talent/post")
    private String type = "all";

    @Pattern(regexp = "relevance|latest", message = "排序仅支持 relevance/latest")
    private String sort = "relevance";

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页最小显示1条")
    private Integer size = 10;
}
