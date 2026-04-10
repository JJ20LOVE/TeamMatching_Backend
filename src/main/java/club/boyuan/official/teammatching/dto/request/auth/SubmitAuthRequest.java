package club.boyuan.official.teammatching.dto.request.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 提交身份认证请求 DTO
 */
@Data
@ApiModel(value = "提交身份认证请求")
public class SubmitAuthRequest {
    
    @ApiModelProperty(value = "学号", required = true, example = "20210001")
    @NotBlank(message = "学号不能为空")
    private String studentId;

    @ApiModelProperty(value = "学校名称（可选；若前端有填写则会同步到用户信息）", required = false, example = "华东师范大学")
    private String school;
    
    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @ApiModelProperty(value = "专业", required = true, example = "计算机科学与技术")
    @NotBlank(message = "专业不能为空")
    private String major;
    
    @ApiModelProperty(value = "年级", required = true, example = "2021 级")
    @NotBlank(message = "年级不能为空")
    private String grade;
    
    @ApiModelProperty(value = "校园邮箱", required = true, example = "zhangsan@stu.edu.cn")
    @NotBlank(message = "校园邮箱不能为空")
    private String email;
    
    @ApiModelProperty(value = "认证材料文件 ID 列表", required = true)
    @NotEmpty(message = "认证材料不能为空")
    private List<Long> materialFileIds;
    
    @ApiModelProperty(value = "材料类型列表（与 fileIds 一一对应）：1-学生证 2-身份证 3-校园卡 4-学信网证明")
    private List<Integer> materialTypes;
}
