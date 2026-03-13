package club.boyuan.official.teammatching.common.enums;

/**
 * 项目状态枚举
 */
public enum ProjectStatusEnum {
    /**
     * 草拟
     */
    DRAFT(0, "草拟"),

    /**
     * 实施
     */
    IN_PROGRESS(1, "实施"),

    /**
     * 招募中
     */
    RECRUITING(2, "招募中"),

    /**
     * 完成
     */
    COMPLETED(3, "完成"),

    /**
     * 终止
     */
    TERMINATED(4, "终止");

    private final int code;
    private final String description;

    ProjectStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValidStatus(Integer status) {
        if (status == null) {
            return false;
        }
        for (ProjectStatusEnum value : values()) {
            if (value.code == status) {
                return true;
            }
        }
        return false;
    }
}