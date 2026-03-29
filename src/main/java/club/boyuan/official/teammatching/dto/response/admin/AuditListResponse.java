package club.boyuan.official.teammatching.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核列表响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditListResponse {

    private Integer total;
    private Integer page;
    private Integer size;
    private List<AuthItemDTO> list;

    /**
     * 待审核人员信息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthItemDTO {
        private Integer authId;
        private String studentId;
        private String realName;
        private String major;
        private String grade;
        private String email;
        private List<MaterialDTO> materials;
        private LocalDateTime applyTime;
    }

    /**
     * 认证材料DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialDTO {
        private Integer materialId;
        private Integer materialType;
        private FileInfoDTO fileInfo;
    }

    /**
     * 文件信息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfoDTO {
        private Long fileId;
        private String fileName;
        private String fileUrl;
        private Long fileSize;
    }
}
