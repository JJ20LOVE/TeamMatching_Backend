package club.boyuan.official.teammatching.dto.request.talent;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新人才卡可见性请求
 */
@Data
public class UpdateTalentStatusRequest {

    @NotNull(message = "isVisible不能为空")
    private Boolean isVisible;
}
