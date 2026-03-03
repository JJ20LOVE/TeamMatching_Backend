package club.boyuan.official.teammatching.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 统一 API 响应封装
 */
@Data
@ApiModel(value = "统一响应 DTO")
public class CommonResponse<T> {
    
    @ApiModelProperty(value = "是否成功", required = true, example = "true")
    private Boolean success;
    
    @ApiModelProperty(value = "状态码", required = true, example = "200")
    private Integer code;
    
    @ApiModelProperty(value = "响应消息", required = true)
    private String message;
    
    @ApiModelProperty(value = "响应数据")
    private T data;
    
    /**
     * 成功响应（无数据）
     */
    public static <T> CommonResponse<T> ok() {
        CommonResponse<T> response = new CommonResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("操作成功");
        return response;
    }
    
    /**
     * 成功响应（有数据）
     */
    public static <T> CommonResponse<T> ok(T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> CommonResponse<T> ok(String message, T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 失败响应
     */
    public static <T> CommonResponse<T> error(Integer code, String message) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
