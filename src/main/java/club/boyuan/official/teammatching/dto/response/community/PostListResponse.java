package club.boyuan.official.teammatching.dto.response.community;

import club.boyuan.official.teammatching.dto.response.CommonResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "帖子列表响应")
public class PostListResponse extends CommonResponse<List<PostListResponse.CommunityPostItem>> {

    public static PostListResponse success(List<CommunityPostItem> list) {
        PostListResponse response = new PostListResponse();
        response.setSuccess(true);
        response.setMessage("success");
        response.setCode(200);
        response.setData(list);
        return response;
    }
    @Data
    @ApiModel(value = "帖子项")
    public static class CommunityPostItem {
        @ApiModelProperty(value = "帖子ID")
        private Integer postId;

        @ApiModelProperty(value = "板块：1-技术交流 2-灵感分享 3-组队经验")
        private Integer section;

        @ApiModelProperty(value = "标题")
        private String title;

        @ApiModelProperty(value = "内容")
        private String content;

        @ApiModelProperty(value = "图片列表")
        private List<String> images;

        @ApiModelProperty(value = "用户信息")
        private UserInfo userInfo;

        @ApiModelProperty(value = "浏览次数")
        private Integer viewCount;

        @ApiModelProperty(value = "点赞数")
        private Integer likeCount;

        @ApiModelProperty(value = "评论数")
        private Integer commentCount;

        @ApiModelProperty(value = "是否置顶")
        private Boolean isTop;

        @ApiModelProperty(value = "是否精华")
        private Boolean isEssence;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        @ApiModelProperty(value = "创建时间")
        private LocalDateTime createdTime;
    }

    @Data
    @ApiModel(value = "用户信息")
    public static class UserInfo {
        @ApiModelProperty(value = "用户ID")
        private Integer userId;

        @ApiModelProperty(value = "昵称")
        private String nickname;

        @ApiModelProperty(value = "头像")
        private String avatar;
    }
}
