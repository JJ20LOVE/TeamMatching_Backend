package club.boyuan.official.teammatching.dto.response.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目角色响应 DTO
 */
@Data
@ApiModel(value = "项目角色响应")
public class ProjectRoleResponse {
    
    @ApiModelProperty(value = "要求 ID", example = "301")
    private Integer requirementId;
    
    @ApiModelProperty(value = "所需角色名", example = "前端开发")
    private String role;
    
    @ApiModelProperty(value = "招募人数", example = "2")
    private Integer memberQuota;
    
    @ApiModelProperty(value = "当前已申请人数", example = "1")
    private Integer currentApplicants;
    
    @ApiModelProperty(value = "当前已加入人数", example = "1")
    private Integer currentMembers;
    
    @ApiModelProperty(value = "具体招募要求", example = "熟悉 Vue.js")
    private String recruitRequirements;
}