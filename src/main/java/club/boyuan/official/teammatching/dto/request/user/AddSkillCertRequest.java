package club.boyuan.official.teammatching.dto.request.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 添加技能认证请求 DTO
 */
@Data
@ApiModel(value = "添加技能认证请求")
public class AddSkillCertRequest {
    
    @ApiModelProperty(value = "技能名称", required = true, example = "Python")
    @NotBlank(message = "技能名称不能为空")
    private String skillName;
    
    @ApiModelProperty(value = "证书名称", required = true, example = "Python 二级证书")
    @NotBlank(message = "证书名称不能为空")
    private String certName;
    
    @ApiModelProperty(value = "证书文件 ID", required = true, example = "10002")
    @NotNull(message = "证书文件 ID 不能为空")
    private Long certFileId;
    
    @ApiModelProperty(value = "发证日期", required = true, example = "2023-06-01")
    @NotNull(message = "发证日期不能为空")
    private LocalDate issueDate;
    
    @ApiModelProperty(value = "发证机构", example = "教育部考试中心")
    private String issuer;
    
    @ApiModelProperty(value = "证书编号", example = "20230601123456")
    private String certNumber;
    
    @ApiModelProperty(value = "证书描述", example = "全国计算机等级考试二级 Python")
    private String description;
}
