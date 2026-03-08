package club.boyuan.official.teammatching.dto.request.community;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建评论请求DTO
 */
public class CreateCommentRequest {
    @NotBlank
    private String content;

    private Integer ParentId;
}