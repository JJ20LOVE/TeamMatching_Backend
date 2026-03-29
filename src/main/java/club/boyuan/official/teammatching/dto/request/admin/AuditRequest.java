package club.boyuan.official.teammatching.dto.request.admin;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核请求DTO
 */
public class AuditRequest {
    // 审核请求参数
    private Integer page = 1;
    private Integer size = 10;

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }
}
