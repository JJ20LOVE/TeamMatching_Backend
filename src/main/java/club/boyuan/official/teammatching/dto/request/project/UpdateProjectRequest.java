package club.boyuan.official.teammatching.dto.request.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新项目请求 DTO
 */
@Data
@ApiModel(value = "更新项目请求")
public class UpdateProjectRequest {
    
    @ApiModelProperty(value = "项目名称", example = "基于 AI 的校园组队平台")
    private String name;
    
    @ApiModelProperty(value = "所属赛道（大创、挑战杯等）", example = "大创")
    private String belongTrack;
    
    @ApiModelProperty(value = "级别：1-校级 2-省级 3-国家级", example = "2")
    private Integer level;
    
    @ApiModelProperty(value = "类型：创新训练/创业实践", example = "创新训练")
    private String projectType;
    
    @ApiModelProperty(value = "项目详细介绍", example = "本项目旨在利用人工智能技术...")
    private String projectIntro;
    
    @ApiModelProperty(value = "项目特点/亮点", example = "智能匹配、实时沟通")
    private String projectFeatures;
    
    @ApiModelProperty(value = "项目标签（逗号分隔）", example = "AI，组队")
    private String tags;
    
    @ApiModelProperty(value = "是否允许跨专业申请", example = "true")
    private Boolean allowCrossMajor;
    
    @ApiModelProperty(value = "是否匿名发布", example = "false")
    private Boolean isAnonymous;
    
    @ApiModelProperty(value = "匿名时显示的临时联系方式", example = "队长微信：captain123")
    private String contactInfo;
    
    @ApiModelProperty(value = "招募截止时间", example = "2024-12-31T23:59:59Z")
    private LocalDateTime deadlineRecruit;
    
    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止", example = "2")
    private Integer status;
    
    @ApiModelProperty(value = "角色要求列表")
    private List<RoleRequirementDTO> roleRequirements;
    
    /**
     * 角色要求 DTO
     */
    @Data
    @ApiModel(value = "角色要求")
    public static class RoleRequirementDTO {
        
        @ApiModelProperty(value = "所需角色名", example = "前端开发")
        private String role;
        
        @ApiModelProperty(value = "招募人数", example = "2")
        private Integer memberQuota;
        
        @ApiModelProperty(value = "具体招募要求", example = "熟悉 Vue.js")
        private String recruitRequirements;
    }
}