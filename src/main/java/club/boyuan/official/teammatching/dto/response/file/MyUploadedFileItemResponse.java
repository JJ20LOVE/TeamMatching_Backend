package club.boyuan.official.teammatching.dto.response.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 当前用户已上传文件列表项（与 GET /common/file/my 文档一致）
 */
@Data
@ApiModel(value = "我的上传文件列表项")
public class MyUploadedFileItemResponse {

    @ApiModelProperty(value = "文件 ID")
    private Long fileId;

    @ApiModelProperty(value = "原始文件名")
    private String fileName;

    @ApiModelProperty(value = "访问 URL")
    private String fileUrl;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "MIME 类型")
    private String fileType;

    @ApiModelProperty(value = "扩展名")
    private String fileExtension;

    @ApiModelProperty(value = "关联类型：1-用户简历 … 8-认证证明材料")
    private Integer targetType;

    @ApiModelProperty(value = "上传时间")
    private String createdTime;
}
