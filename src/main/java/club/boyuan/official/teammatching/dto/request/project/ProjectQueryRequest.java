package club.boyuan.official.teammatching.dto.request.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目查询请求 DTO（项目广场、项目列表筛选）
 */
@Data
@ApiModel(value = "项目查询请求")
public class ProjectQueryRequest {

    @ApiModelProperty(value = "所属赛道筛选", example = "大创")
    private String track;

    @ApiModelProperty(value = "所需角色筛选", example = "前端开发")
    private String role;

    @ApiModelProperty(value = "排序：latest(最新) hot(最热) deadline(截止最近)",
            allowableValues = "latest,hot,deadline", example = "latest")
    private String sort;

    @ApiModelProperty(value = "搜索关键词", example = "AI 组队")
    private String keyword;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer size;
}