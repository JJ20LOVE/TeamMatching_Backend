package club.boyuan.official.teammatching.dto.response.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件上传响应 DTO
 */
@Data
@ApiModel(value = "文件上传响应 DTO")
public class FileUploadResponse {
    
    @ApiModelProperty(value = "文件ID")
    private Long fileId;
    
    @ApiModelProperty(value = "原始文件名")
    private String fileName;
    
    @ApiModelProperty(value = "文件访问 URL")
    private String fileUrl;
    
    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;
    
    @ApiModelProperty(value = "文件 MIME 类型")
    private String fileType;
    
    @ApiModelProperty(value = "文件扩展名")
    private String fileExtension;
    
    @ApiModelProperty(value = "文件 MD5 值")
    private String md5Hash;
}
