package club.boyuan.official.teammatching.dto.response.admin;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
 * 统计数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    // 统计数据响应数据
    private Integer dau;
    private Integer newUsers;
    private Integer projectsPublished;
    private Integer teamsFormed;
    private Number contentAuditPassRate;
}