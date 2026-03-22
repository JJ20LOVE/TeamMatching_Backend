package club.boyuan.official.teammatching.dto.response.talent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的卡片详情
 */
@Data
public class TalentDetailResponse {

    private Integer cardId;

    private Integer userId;

    private Integer status;

    private Boolean isVisible;

    private String displayName;

    private String major;

    private String grade;

    private String cardTitle;

    private String targetDirection;

    private String expectedCompetition;

    private String expectedRole;

    private String selfStatement;

    private String skillTags;

    private String resumeUrl;

    private String portfolioUrl;

    private String githubUrl;

    private Integer viewCount;

    private Integer inviteCount;

    private LocalDateTime createdTime;
}
