package club.boyuan.official.teammatch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID，主键
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 学号（支持字母+数字）
     */
    private String studentId;

    /**
     * 用户姓名，真实姓名
     */
    private String username;

    /**
     * 用户昵称，可重复
     */
    private String nickname;

    /**
     * 微信OpenID（用于一键登录）
     */
    private String openid;

    /**
     * 微信UnionID（多平台关联）
     */
    private String unionid;

    /**
     * 微信原始昵称
     */
    private String wechatNickname;

    /**
     * 手机号，加密存储
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 密码，加密存储（BCrypt）
     */
    private String password;

    /**
     * 头像URL（OSS/CDN链接）
     */
    private String avatar;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDateTime birthday;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级（如：2021级）
     */
    private String grade;

    /**
     * 技术栈（如"Python,Java,机器学习"）
     */
    private String techStack;

    /**
     * 个人简介
     */
    private String personalIntro;

    /**
     * 获奖经历
     */
    private String awardExperience;

    /**
     * 通用简历附件URL
     */
    private String resumeUrl;

    /**
     * 通用简历文件名
     */
    private String resumeName;

    /**
     * 简历上传时间
     */
    private LocalDateTime resumeUploadTime;

    /**
     * 角色：admin/student
     */
    private String role;

    /**
     * 认证状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer authStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID，关联user表
     */
    private Integer auditorUserId;

    /**
     * 审核备注/驳回原因
     */
    private String remark;

    /**
     * 人才卡片是否可见（快捷开关）
     */
    private Boolean isTalentVisible;

    /**
     * 当前使用的人才卡片ID
     */
    private Integer talentCardId;

    /**
     * 是否冻结：TRUE-冻结 FALSE-正常
     */
    private Boolean status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 注册时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}