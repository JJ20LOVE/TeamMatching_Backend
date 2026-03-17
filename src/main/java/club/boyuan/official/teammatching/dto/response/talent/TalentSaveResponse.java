package club.boyuan.official.teammatching.dto.response.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建/更新人才卡返回
 */
@Data
public class TalentSaveResponse {

    private Integer cardId;

    private Integer status;

    private LocalDateTime createTime;
}
