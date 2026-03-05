package club.boyuan.official.teammatching.dto.response.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件删除响应 DTO
 */
@Data
@ApiModel(value = "文件删除响应 DTO")
public class FileDeleteResponse {
    
    @ApiModelProperty(value = "提示信息")
    private String message;
}
