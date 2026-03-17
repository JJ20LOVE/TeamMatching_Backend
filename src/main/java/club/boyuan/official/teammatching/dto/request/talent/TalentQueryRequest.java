package club.boyuan.official.teammatching.dto.request.talent;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 人才列表查询参数
 */
@Data
public class TalentQueryRequest {

    private String major;

    private String grade;

    private String skillTags;

    private String expectedCompetition;

    /**
     * latest / hot / recommend
     */
    private String sort;

    /**
     * 兼容 source 项目参数命名
     */
    @Min(value = 1, message = "current最小为1")
    private Integer current;

    /**
     * 兼容本项目常见参数命名
     */
    @Min(value = 1, message = "page最小为1")
    private Integer page;

    @Min(value = 1, message = "size最小为1")
    private Integer size;
}
