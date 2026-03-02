package club.boyuan.official.teammatching.dto.response.community;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 帖子详情响应DTO
 */
@Data
public class PostDetailResponse<T> {
    private Integer code;
    private String message;
    private T data;
    
    @Data
    @AllArgsConstructor
    public static class PostCreateVO { // 注意必须是 static
        private Long postId;
        private String message;
    }

    public static <T> PostDetailResponse<T> success(T data) {
        PostDetailResponse<T> result = new PostDetailResponse<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
}