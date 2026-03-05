package club.boyuan.official.teammatching.dto.response.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料响应 DTO
 */
@Data
@ApiModel(value = "用户资料响应")
public class UserProfileResponse {
    
    @ApiModelProperty(value = "用户 ID")
    private Integer userId;
    
    @ApiModelProperty(value = "学号")
    private String studentId;
    
    @ApiModelProperty(value = "用户姓名")
    private String username;
    
    @ApiModelProperty(value = "用户昵称")
    private String nickname;
    
    @ApiModelProperty(value = "头像文件信息")
    private AvatarFileDTO avatarFile;
    
    @ApiModelProperty(value = "性别：0-未知 1-男 2-女")
    private Integer gender;
    
    @ApiModelProperty(value = "出生日期")
    private LocalDate birthday;
    
    @ApiModelProperty(value = "专业")
    private String major;
    
    @ApiModelProperty(value = "年级")
    private String grade;
    
    @ApiModelProperty(value = "邮箱")
    private String email;
    
    @ApiModelProperty(value = "手机号（脱敏）")
    private String phone;
    
    @ApiModelProperty(value = "技术栈")
    private String techStack;
    
    @ApiModelProperty(value = "个人简介")
    private String personalIntro;
    
    @ApiModelProperty(value = "获奖经历")
    private String awardExperience;
    
    @ApiModelProperty(value = "认证状态：0-待审核 1-已通过 2-已驳回")
    private Integer authStatus;
    
    @ApiModelProperty(value = "人才卡片是否可见")
    private Boolean isTalentVisible;
    
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;
    
    /**
     * 头像文件 DTO
     */
    @Data
    @ApiModel(value = "头像文件信息")
    public static class AvatarFileDTO {
        @ApiModelProperty(value = "文件 ID")
        private Long fileId;
        
        @ApiModelProperty(value = "文件名")
        private String fileName;
        
        @ApiModelProperty(value = "文件 URL")
        private String fileUrl;
        
        @ApiModelProperty(value = "文件大小")
        private Long fileSize;
    }
}