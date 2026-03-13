package club.boyuan.official.teammatching.common.enums;

/**
 * 申请结果枚举
 */
public enum ApplicationResultEnum {
    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 通过
     */
    APPROVED(1, "通过"),

    /**
     * 驳回
     */
    REJECTED(2, "驳回");

    private final int code;
    private final String description;

    ApplicationResultEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}