package club.boyuan.official.teammatching.dto.response.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情响应 DTO
 */
@Data
@ApiModel(value = "项目详情响应")
public class ProjectDetailResponse {
    
    @ApiModelProperty(value = "项目 ID", example = "201")
    private Integer projectId;
    
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
    
    @ApiModelProperty(value = "发布人信息")
    private PublisherInfo publisherInfo;
    
    @ApiModelProperty(value = "招募截止时间", example = "2024-12-31T23:59:59Z")
    private LocalDateTime deadlineRecruit;
    
    @ApiModelProperty(value = "项目状态：0-草拟 1-实施 2-招募中 3-完成 4-终止", example = "2")
    private Integer status;
    
    @ApiModelProperty(value = "审核状态：0-待审核 1-通过 2-驳回", example = "1")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "浏览次数", example = "156")
    private Integer viewCount;
    
    @ApiModelProperty(value = "收藏次数", example = "23")
    private Integer favoriteCount;
    
    @ApiModelProperty(value = "申请人数", example = "8")
    private Integer applyCount;
    
    @ApiModelProperty(value = "发布时间", example = "2024-01-01T10:00:00Z")
    private LocalDateTime releaseTime;
    
    @ApiModelProperty(value = "角色要求列表")
    private List<ProjectRoleInfo> roleRequirements;
    
    /**
     * 项目角色信息 DTO（用于详情响应）
     */
    @Data
    @ApiModel(value = "项目角色信息")
    public static class ProjectRoleInfo {
        
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
    
    /**
     * 发布人信息 DTO
     */
    @Data
    @ApiModel(value = "发布人信息")
    public static class PublisherInfo {
        
        @ApiModelProperty(value = "用户 ID", example = "10001")
        private Integer userId;
        
        @ApiModelProperty(value = "昵称", example = "张三")
        private String nickname;
        
        @ApiModelProperty(value = "头像 URL", example = "https://example.com/avatar.jpg")
        private String avatar;
    }
}