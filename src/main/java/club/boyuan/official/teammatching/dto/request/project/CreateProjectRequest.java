package club.boyuan.official.teammatching.dto.request.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建项目请求 DTO
 */
@Data
@ApiModel(value = "创建项目请求")
public class CreateProjectRequest {
    
    @NotBlank(message = "项目名称不能为空")
    @ApiModelProperty(value = "项目名称", required = true, example = "基于 AI 的校园组队平台")
    private String name;
    
    @NotBlank(message = "所属赛道不能为空")
    @ApiModelProperty(value = "所属赛道（大创、挑战杯等）", required = true, example = "大创")
    private String belongTrack;
    
    @NotNull(message = "级别不能为空")
    @ApiModelProperty(value = "级别：1-校级 2-省级 3-国家级", required = true, example = "2")
    private Integer level;
    
    @ApiModelProperty(value = "类型：创新训练/创业实践", example = "创新训练")
    private String projectType;
    
    @NotBlank(message = "项目介绍不能为空")
    @ApiModelProperty(value = "项目详细介绍", required = true, example = "本项目旨在利用人工智能技术，为大学生提供一个高效、精准的比赛组队平台")
    private String projectIntro;

    @ApiModelProperty(value = "项目进展说明")
    private String projectProgress;
    
    @ApiModelProperty(value = "项目特点/亮点", example = "智能匹配、实时沟通、团队协作")
    private String projectFeatures;
    
    @ApiModelProperty(value = "项目标签（逗号分隔）", example = "AI，组队，校园")
    private String tags;

    @ApiModelProperty(value = "项目说明等附件文件 ID（先通过 /common/upload/file 上传，建议 targetType 与项目材料一致）", example = "10006")
    private Long attachmentFileId;

    @ApiModelProperty(value = "是否允许跨专业申请", example = "true")
    private Boolean allowCrossMajor = false;
    
    @ApiModelProperty(value = "是否匿名发布", example = "false")
    private Boolean isAnonymous = false;
    
    @ApiModelProperty(value = "匿名时显示的临时联系方式", example = "队长微信：captain123")
    private String contactInfo;
    
    @NotNull(message = "招募截止时间不能为空")
    @ApiModelProperty(value = "招募截止时间", required = true, example = "2024-12-31T23:59:59Z")
    private LocalDateTime deadlineRecruit;
    
    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止（默认为 0-草拟）", example = "0")
    private Integer status = 0;
    
    @ApiModelProperty(value = "角色要求列表")
    private List<RoleRequirementDTO> roleRequirements;
    
    /**
     * 角色要求 DTO
     */
    @Data
    @ApiModel(value = "角色要求")
    public static class RoleRequirementDTO {
        
        @NotBlank(message = "角色名不能为空")
        @ApiModelProperty(value = "所需角色名", required = true, example = "前端开发")
        private String role;
        
        @NotNull(message = "招募人数不能为空")
        @ApiModelProperty(value = "招募人数", required = true, example = "2")
        private Integer memberQuota;
        
        @ApiModelProperty(value = "具体招募要求", example = "熟悉 Vue.js，有小程序开发经验")
        private String recruitRequirements;
    }
}