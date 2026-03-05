package club.boyuan.official.teammatching.dto.request.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 更新资料请求 DTO
 */
@Data
@ApiModel(value = "更新个人资料请求")
public class UpdateProfileRequest {
    
    @ApiModelProperty(value = "用户昵称")
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;
    
    @ApiModelProperty(value = "头像文件 ID（通过上传接口获取，targetType=7）")
    private Long avatarFileId;
    
    @ApiModelProperty(value = "性别：0-未知 1-男 2-女")
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer gender;
    
    @ApiModelProperty(value = "出生日期")
    private LocalDate birthday;
    
    @ApiModelProperty(value = "专业")
    @Size(max = 100, message = "专业名称长度不能超过 100 个字符")
    private String major;
    
    @ApiModelProperty(value = "年级")
    @Size(max = 50, message = "年级名称长度不能超过 50 个字符")
    private String grade;
    
    @ApiModelProperty(value = "技术栈（逗号分隔）")
    @Size(max = 500, message = "技术栈描述长度不能超过 500 个字符")
    private String techStack;
    
    @ApiModelProperty(value = "个人简介")
    @Size(max = 1000, message = "个人简介长度不能超过 1000 个字符")
    private String personalIntro;
    
    @ApiModelProperty(value = "获奖经历")
    @Size(max = 2000, message = "获奖经历描述长度不能超过 2000 个字符")
    private String awardExperience;
}