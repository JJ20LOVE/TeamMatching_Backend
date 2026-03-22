package club.boyuan.official.teammatching.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审核认证请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditVerifyRequest {

    /**
     * 审核结果：1-通过 2-驳回
     */
    @NotNull(message = "审核结果不能为空")
    private Integer result;

    /**
     * 整体审核备注/驳回原因
     */
    private String remark;

    /**
     * 材料逐项审核结果（可选）
     */
    private List<MaterialResultDTO> materialResults;

    /**
     * 材料审核结果DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialResultDTO {

        /**
         * 材料ID
         */
        private Integer materialId;

        /**
         * 审核结果：1-通过 2-驳回
         */
        private Integer result;

        /**
         * 材料审核备注
         */
        private String remark;
    }
}
