package club.boyuan.official.teammatching.dto.request.talent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 发送人才邀请请求
 */
@Data
public class TalentInviteRequest {

    @NotNull(message = "talentCardId不能为空")
    private Integer talentCardId;

    /**
     * 兼容旧版单选项目ID
     */
    private Integer projectId;

    /**
     * 新版可多选项目ID
     */
    private List<Integer> projectIds;

    @NotBlank(message = "projectRole不能为空")
    private String projectRole;

    private String invitationMessage;

    /**
     * 是否手动输入项目名称（true 时使用 customProjectName，忽略 projectId/projectIds）
     */
    private Boolean hasManualInput;

    /**
     * 手动输入项目名称（hasManualInput=true 时必填）
     */
    private String customProjectName;

    /**
     * 邀请附加联系方式
     */
    private String contactInfo;
}
