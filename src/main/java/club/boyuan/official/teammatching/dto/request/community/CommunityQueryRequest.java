package club.boyuan.official.teammatching.dto.request.community;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 社区查询请求DTO
 */
@Data
public class CommunityQueryRequest {
    /**
     * 板块筛选
     * 前端传 1, 2, 3 即可自动映射为对应的枚举常量
     */
    private SectionType section;

    @Getter
    @AllArgsConstructor
    public enum SectionType {
        TECH_EXCHANGE(1, "技术交流"),
        INSPIRATION_SHARING(2, "灵感分享"),
        TEAM_EXPERIENCE(3, "组队经验");

        @JsonValue // 核心：告诉 Jackson 使用这个字段进行 JSON 解析和序列化
        private final int code;
        private final String description;
    }

    private OrderType type;

    @Getter
    @AllArgsConstructor
    public enum OrderType {
        RECOMMEND("recommend"),
        LATEST("latest"),
        HOTTEST("hottest");

        @JsonValue // 告诉 Jackson 前端传来的字符串（如"recommend"）对应哪个枚举
        private final String value;
    }

    private String keyword;
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;
    @Min(value = 1, message = "每页最小显示1条")
    private Integer size = 10;
}