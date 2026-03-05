package club.boyuan.official.teammatching.dto.response.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询认证状态响应 DTO
 */
@Data
@ApiModel(value = "查询认证状态响应")
public class AuthStatusResponse {
    
    @ApiModelProperty(value = "认证状态：0-待审核 1-已通过 2-已驳回")
    private Integer authStatus;
    
    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核备注/驳回原因")
    private String remark;
    
    @ApiModelProperty(value = "认证材料列表")
    private List<AuthMaterialInfo> materials;
    
    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;
    
    /**
     * 认证材料信息
     */
    @Data
    @ApiModel(value = "认证材料信息")
    public static class AuthMaterialInfo {
        
        @ApiModelProperty(value = "材料 ID")
        private Integer materialId;
        
        @ApiModelProperty(value = "材料类型：1-学生证 2-身份证 3-校园卡 4-学信网证明")
        private Integer materialType;
        
        @ApiModelProperty(value = "文件信息")
        private FileInfo fileInfo;
        
        @ApiModelProperty(value = "材料审核状态：0-待审核 1-已通过 2-已驳回")
        private Integer auditStatus;
        
        @ApiModelProperty(value = "审核备注")
        private String remark;
        
        @ApiModelProperty(value = "创建时间")
        private LocalDateTime createdTime;
    }
    
    /**
     * 文件信息
     */
    @Data
    @ApiModel(value = "文件信息")
    public static class FileInfo {
        
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
