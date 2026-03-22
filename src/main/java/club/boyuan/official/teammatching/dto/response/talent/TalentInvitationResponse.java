package club.boyuan.official.teammatching.dto.response.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人才邀请返回
 */
@Data
public class TalentInvitationResponse {

    private Integer invitationId;

    private Integer captainId;

    private Integer talentId;

    private Integer projectId;

    private Integer talentCardId;

    private String projectName;

    private String projectRole;

    private String invitationMessage;

    private Integer status;

    private LocalDateTime sendTime;
}
