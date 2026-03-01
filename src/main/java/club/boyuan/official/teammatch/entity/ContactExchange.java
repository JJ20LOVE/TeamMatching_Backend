package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 联系方式交换记录表实体类
 */
@Data
@TableName("contact_exchange")
public class ContactExchange {

    /**
     * 交换ID，主键
     */
    @TableId(value = "exchange_id", type = IdType.AUTO)
    private Integer exchangeId;

    /**
     * 发起方ID，关联user表
     */
    private Integer requesterId;

    /**
     * 接收方ID，关联user表
     */
    private Integer receiverId;

    /**
     * 关联项目ID
     */
    private Integer projectId;

    /**
     * 状态：0-待确认 1-已同意 2-已拒绝
     */
    private Integer status;

    /**
     * 请求时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime requestTime;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;
}