package club.boyuan.official.teammatching.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核认证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditVerifyResponse {

    /**
     * 响应消息
     */
    private String message;

    /**
     * 审核后的认证状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer authStatus;
}
