package club.boyuan.official.teammatching.common.enums;

/**
 * 认证状态枚举
 */
public enum AuthStatusEnum {
    
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
    /**
     * 已驳回
     */
    REJECTED(2, "已驳回");
    
    private final Integer code;
    private final String desc;
    
    AuthStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static AuthStatusEnum getByCode(Integer code) {
        for (AuthStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}