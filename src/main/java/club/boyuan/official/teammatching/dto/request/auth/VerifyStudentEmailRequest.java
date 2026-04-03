package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 学生邮箱认证请求 DTO
 */
@Data
@ApiModel(value = "学生邮箱认证请求")
public class VerifyStudentEmailRequest {

    @ApiModelProperty(value = "学生邮箱（仅支持 *.edu.cn）", required = true, example = "zhangsan@stu.edu.cn")
    @NotBlank(message = "学生邮箱不能为空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.edu\\.cn$", message = "必须使用以 .edu.cn 结尾的学生邮箱")
    private String email;

    @ApiModelProperty(value = "6 位验证码", required = true, example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String verifyCode;

    @ApiModelProperty(value = "学校名称", required = true, example = "华东师范大学")
    @NotBlank(message = "学校名称不能为空")
    private String school;
}

