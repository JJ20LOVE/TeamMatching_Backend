package club.boyuan.official.teammatching.mq.enums;

/**
 * 通知类型（与「通知设置」四类开关语义对齐）
 */
public enum NotificationCategory {

    /** 新消息 / 聊天相关 */
    MESSAGE,

    /** 项目状态变更 */
    PROJECT_UPDATE,

    /** 组队邀请 */
    INVITATION,

    /** 系统公告、审核结果等 */
    SYSTEM
}
