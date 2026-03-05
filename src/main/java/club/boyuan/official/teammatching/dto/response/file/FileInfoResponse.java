package club.boyuan.official.teammatching.dto.response.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件信息响应 DTO
 */
@Data
@ApiModel(value = "文件信息响应 DTO")
public class FileInfoResponse {
    
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
    
    @ApiModelProperty(value = "关联类型：1-用户简历 2-技能认证证书 3-帖子图片 4-评论图片 5-项目申请附件 6-人才卡片附件 7-用户头像 8-认证证明材料")
    private Integer targetType;
    
    @ApiModelProperty(value = "关联业务 ID")
    private Integer targetId;
    
    @ApiModelProperty(value = "是否临时文件")
    private Boolean isTemp;
    
    @ApiModelProperty(value = "上传用户 ID")
    private Integer userId;
    
    @ApiModelProperty(value = "上传时间")
    private String createdTime;
}
