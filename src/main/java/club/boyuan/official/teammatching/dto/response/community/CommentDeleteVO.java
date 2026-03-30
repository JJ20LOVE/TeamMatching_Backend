package club.boyuan.official.teammatching.dto.response.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDeleteVO {
    private Boolean deleted;
    private Integer commentId;
}
