package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * <p>
 * 会话表
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_session")
@ApiModel(value="ChatSession对象", description="会话表")
public class ChatSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会话ID，主键")
    @TableId(value = "session_id", type = IdType.AUTO)
    private Integer sessionId;

    @ApiModelProperty(value = "参与者1，关联user表")
    private Integer user1Id;

    @ApiModelProperty(value = "参与者2，关联user表")
    private Integer user2Id;

    @ApiModelProperty(value = "关联项目ID（如果是项目相关聊天）")
    private Integer projectId;

    @ApiModelProperty(value = "最后一条消息内容")
    private String lastMessage;

    @ApiModelProperty(value = "最后消息时间")
    private LocalDateTime lastMsgTime;

    @ApiModelProperty(value = "用户1未读数")
    private Integer user1Unread;

    @ApiModelProperty(value = "用户2未读数")
    private Integer user2Unread;

    @ApiModelProperty(value = "招募沟通状态：communicating/offer/reject")
    @TableField("recruit_status")
    private String recruitStatus;

    @ApiModelProperty(value = "状态：1-正常 0-删除")
    private Integer status;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
