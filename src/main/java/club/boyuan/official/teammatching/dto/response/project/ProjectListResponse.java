package club.boyuan.official.teammatching.dto.response.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目列表响应 DTO（项目广场、相似项目、智能匹配统一结构）
 */
@Data
@ApiModel(value = "项目列表项")
public class ProjectListResponse {

    @ApiModelProperty(value = "项目ID", example = "201")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称", example = "基于AI的校园组队平台")
    private String name;

    @ApiModelProperty(value = "所属赛道（大创、挑战杯等）", example = "大创")
    private String belongTrack;

    @ApiModelProperty(value = "项目简介", example = "本项目旨在利用人工智能技术，为大学生提供一个高效、精准的比赛组队平台")
    private String projectIntro;

    @ApiModelProperty(value = "发布人信息")
    private PublisherInfo publisherInfo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @ApiModelProperty(value = "招募截止时间", example = "2024-12-31T23:59:59Z")
    private LocalDateTime deadlineRecruit;

    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止", example = "2")
    private Integer status;

    @ApiModelProperty(value = "浏览次数", example = "156")
    private Integer viewCount;

    @ApiModelProperty(value = "收藏次数", example = "23")
    private Integer favoriteCount;

    @ApiModelProperty(value = "当前用户是否已向该项目投递过（立即沟通）；未登录为 false", example = "false")
    private Boolean hasApplied;

    @ApiModelProperty(value = "角色汇总信息列表")
    private List<RoleSummary> roleSummaries;

    /**
     * 发布人信息
     */
    @Data
    @ApiModel(value = "发布人信息（列表）")
    public static class PublisherInfo {

        @ApiModelProperty(value = "昵称", example = "张三")
        private String nickname;

        @ApiModelProperty(value = "头像", example = "https://example.com/avatar.jpg")
        private String avatar;
    }

    /**
     * 角色汇总
     */
    @Data
    @ApiModel(value = "角色汇总信息")
    public static class RoleSummary {

        @ApiModelProperty(value = "角色名", example = "前端开发")
        private String role;

        @ApiModelProperty(value = "招募人数", example = "2")
        private Integer memberQuota;

        @ApiModelProperty(value = "当前已加入人数", example = "1")
        private Integer currentMembers;
    }
}
