package club.boyuan.official.teammatching.dto.response.community;

import club.boyuan.official.teammatching.dto.response.CommonResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论专用的响应类
 * 继承后，它本质上就是 CommonResponse<BaseResultVO>，但名字更短
 */


@Data
@AllArgsConstructor
public class CommentCreateVO {
    private Long commentId;
    private String message;
}