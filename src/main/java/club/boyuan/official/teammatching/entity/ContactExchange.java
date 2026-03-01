package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 联系方式交换记录表
 * </p>
 *
 * @author dhy
 * @since 2026-03-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("contact_exchange")
@ApiModel(value="ContactExchange对象", description="联系方式交换记录表")
public class ContactExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "交换ID，主键")
    @TableId(value = "exchange_id", type = IdType.AUTO)
    private Integer exchangeId;

    @ApiModelProperty(value = "发起方ID，关联user表")
    private Integer requesterId;

    @ApiModelProperty(value = "接收方ID，关联user表")
    private Integer receiverId;

    @ApiModelProperty(value = "关联项目ID")
    private Integer projectId;

    @ApiModelProperty(value = "状态：0-待确认 1-已同意 2-已拒绝")
    private Integer status;

    @ApiModelProperty(value = "请求时间")
    private LocalDateTime requestTime;

    @ApiModelProperty(value = "响应时间")
    private LocalDateTime responseTime;


}
