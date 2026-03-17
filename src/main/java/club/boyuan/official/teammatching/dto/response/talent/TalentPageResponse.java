package club.boyuan.official.teammatching.dto.response.talent;

import lombok.Data;

import java.util.List;

/**
 * 人才分页响应
 */
@Data
public class TalentPageResponse<T> {

    private List<T> records;

    private Long total;

    private Long size;

    private Long current;

    private Long pages;

    public static <T> TalentPageResponse<T> of(List<T> records, Long total, Long size, Long current, Long pages) {
        TalentPageResponse<T> response = new TalentPageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setSize(size);
        response.setCurrent(current);
        response.setPages(pages);
        return response;
    }
}
