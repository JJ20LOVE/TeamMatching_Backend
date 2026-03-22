package club.boyuan.official.teammatching.dto.request.talent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送人才邀请请求
 */
@Data
public class TalentInviteRequest {

    @NotNull(message = "talentCardId不能为空")
    private Integer talentCardId;

    @NotNull(message = "projectId不能为空")
    private Integer projectId;

    @NotBlank(message = "projectRole不能为空")
    private String projectRole;

    private String invitationMessage;
}
