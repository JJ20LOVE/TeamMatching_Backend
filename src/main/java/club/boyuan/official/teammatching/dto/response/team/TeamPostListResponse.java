package club.boyuan.official.teammatching.dto.response.team;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队帖子列表响应DTO
 */
@Data
@ApiModel(value = "团队帖子分页响应")
public class TeamPostListResponse {
    @ApiModelProperty(value = "帖子列表")
    private List<PostItem> list;

    @ApiModelProperty(value = "总条数")
    private Long total;

    @ApiModelProperty(value = "页码")
    private Integer page;

    @ApiModelProperty(value = "每页大小")
    private Integer size;

    @Data
    @ApiModel(value = "团队帖子")
    public static class PostItem {
        @ApiModelProperty(value = "帖子ID")
        private Integer postId;

        @ApiModelProperty(value = "项目ID")
        private Integer projectId;

        @ApiModelProperty(value = "标题")
        private String title;

        @ApiModelProperty(value = "内容")
        private String content;

        @ApiModelProperty(value = "作者ID")
        private Integer userId;

        @ApiModelProperty(value = "作者昵称")
        private String nickname;

        @ApiModelProperty(value = "作者头像URL")
        private String avatar;

        @ApiModelProperty(value = "附件URL列表")
        private List<String> attachments;

        @ApiModelProperty(value = "发布时间")
        private LocalDateTime createdTime;
    }
}
