package club.boyuan.official.teammatching.dto.request.talent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/更新人才卡请求
 */
@Data
public class CreateTalentCardRequest {

    /**
     * 状态：0-关闭 1-开启
     */
    @NotNull(message = "status不能为空")
    private Integer status;

    private String displayName;

    @NotBlank(message = "cardTitle不能为空")
    private String cardTitle;

    @NotBlank(message = "targetDirection不能为空")
    private String targetDirection;

    @NotBlank(message = "expectedCompetition不能为空")
    private String expectedCompetition;

    @NotBlank(message = "expectedRole不能为空")
    private String expectedRole;

    private String selfStatement;

    private String skillTags;

    private String resumeUrl;

    private String portfolioUrl;

    private String githubUrl;
}
