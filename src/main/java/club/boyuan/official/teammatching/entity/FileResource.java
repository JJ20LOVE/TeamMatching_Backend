package club.boyuan.official.teammatching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文件资源表（统一管理所有上传文件）
 * </p>
 *
 * @author dhy
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_resource")
@ApiModel(value="FileResource对象", description="文件资源表（统一管理所有上传文件）")
public class FileResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件ID，主键")
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    @ApiModelProperty(value = "原始文件名")
    private String fileName;

    @ApiModelProperty(value = "存储key（OSS路径/对象存储路径）")
    private String fileKey;

    @ApiModelProperty(value = "访问URL（CDN/OSS访问地址）")
    private String fileUrl;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "MIME类型（如：image/jpeg, application/pdf）")
    private String fileType;

    @ApiModelProperty(value = "文件扩展名（如：jpg, pdf）")
    private String fileExtension;

    @ApiModelProperty(value = "文件MD5值（用于去重校验）")
    private String md5Hash;

    @ApiModelProperty(value = "上传用户ID，关联user表")
    private Integer userId;

    @ApiModelProperty(value = "关联类型：1-用户简历 2-技能认证证书 3-帖子图片 4-评论图片 5-项目申请附件 6-人才卡片附件 7-用户头像 8-认证证明材料")
    private Integer targetType;

    @ApiModelProperty(value = "关联业务ID（如skill_cert_id, post_id等）")
    private Integer targetId;

    @ApiModelProperty(value = "是否临时文件（用于清理未关联的临时文件）")
    private Boolean isTemp;

    @ApiModelProperty(value = "是否删除标记（软删除）")
    private Boolean isDeleted;

    @ApiModelProperty(value = "上传时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "删除时间")
    private LocalDateTime deletedTime;


}
