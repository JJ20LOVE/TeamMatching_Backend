package club.boyuan.official.teammatching.dto.request.community;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;
    @NotNull(message = "目标ID不能为空")
    private Integer targetId;
}
