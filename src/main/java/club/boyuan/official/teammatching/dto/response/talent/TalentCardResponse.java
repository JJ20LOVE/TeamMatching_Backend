package club.boyuan.official.teammatching.dto.response.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人才列表卡片项
 */
@Data
public class TalentCardResponse {

    private Integer cardId;

    private Integer userId;

    private String displayName;

    private String major;

    private String grade;

    private String cardTitle;

    private String targetDirection;

    private String expectedCompetition;

    private String skillTags;

    private Integer viewCount;

    private Integer inviteCount;

    private LocalDateTime createdTime;

    private Boolean isFollowing;
}
