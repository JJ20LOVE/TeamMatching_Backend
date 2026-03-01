package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 团队成员表实体类
 */
@Data
@TableName("team_member")
public class TeamMember {

    /**
     * 成员关联ID，主键
     */
    @TableId(value = "team_member_id", type = IdType.AUTO)
    private Integer teamMemberId;

    /**
     * 项目ID，关联project表
     */
    private Integer projectId;

    /**
     * 用户ID，关联user表
     */
    private Integer userId;

    /**
     * 在团队中的角色
     */
    private String role;

    /**
     * 加入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;

    /**
     * 状态：0-在队 1-已退出 2-被移除
     */
    private Integer status;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}