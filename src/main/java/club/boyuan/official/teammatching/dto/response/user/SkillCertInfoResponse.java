package club.boyuan.official.teammatching.dto.response.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 技能认证信息响应 DTO
 */
@Data
@ApiModel(value = "技能认证信息")
public class SkillCertInfoResponse {
    
    @ApiModelProperty(value = "认证 ID")
    private Integer certId;
    
    @ApiModelProperty(value = "技能名称")
    private String skillName;
    
    @ApiModelProperty(value = "证书名称")
    private String certName;
    
    @ApiModelProperty(value = "证书文件信息")
    private CertFileInfo certFile;
    
    @ApiModelProperty(value = "发证日期")
    private LocalDate issueDate;
    
    @ApiModelProperty(value = "发证机构")
    private String issuer;
    
    @ApiModelProperty(value = "证书编号")
    private String certNumber;
    
    @ApiModelProperty(value = "证书描述")
    private String description;
    
    @ApiModelProperty(value = "审核状态：0-待审核 1-已通过 2-已驳回")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;
    
    /**
     * 证书文件信息
     */
    @Data
    @ApiModel(value = "证书文件信息")
    public static class CertFileInfo {
        
        @ApiModelProperty(value = "文件 ID")
        private Long fileId;
        
        @ApiModelProperty(value = "文件名")
        private String fileName;
        
        @ApiModelProperty(value = "文件访问 URL")
        private String fileUrl;
        
        @ApiModelProperty(value = "文件大小（字节）")
        private Long fileSize;
    }
}
