package club.boyuan.official.teammatching.common.enums;

/**
 * 成员状态枚举
 */
public enum TeamMemberStatusEnum {
    IN_TEAM(0, "在队"),
    QUIT(1, "已退出"),
    REMOVED(2, "被移除");

    private final int code;
    private final String description;

    TeamMemberStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TeamMemberStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeamMemberStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static boolean isActive(Integer code) {
        return IN_TEAM.code == code;
    }
}
