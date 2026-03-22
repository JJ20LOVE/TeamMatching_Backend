package club.boyuan.official.teammatching.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容审核请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentVerifyRequest {

    /**
     * 审核结果：1-通过 2-驳回
     * 理由：驳回/下架原因
     */
    @NotNull
    private Integer result;

    private String reason;
}