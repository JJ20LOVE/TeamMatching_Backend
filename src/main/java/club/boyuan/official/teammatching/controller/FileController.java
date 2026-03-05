package club.boyuan.official.teammatching.controller;

import club.boyuan.official.teammatching.common.annotation.NeedLogin;
import club.boyuan.official.teammatching.dto.response.CommonResponse;
import club.boyuan.official.teammatching.dto.response.file.FileDeleteResponse;
import club.boyuan.official.teammatching.dto.response.file.FileInfoResponse;
import club.boyuan.official.teammatching.dto.response.file.FileUploadResponse;
import club.boyuan.official.teammatching.entity.SkillTag;
import club.boyuan.official.teammatching.exception.BusinessException;
import club.boyuan.official.teammatching.mapper.SkillTagMapper;
import club.boyuan.official.teammatching.service.FileService;
import club.boyuan.official.teammatching.common.utils.UserContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Api(tags = "通用功能接口")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    private final SkillTagMapper skillTagMapper;
    
    /**
     * 通用文件上传接口
     * @param file 上传的文件
     * @param targetType 关联类型
     * @param isTemp 是否临时文件
     * @return 文件上传响应
     */
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "通用文件上传", notes = "上传任意类型文件，返回文件信息")
    @NeedLogin
    public ResponseEntity<CommonResponse<FileUploadResponse>> uploadFile(
            @ApiParam(value = "文件（支持 jpg/png/gif/pdf/doc/zip 等，最大 50MB）", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "关联类型：1-用户简历 2-技能认证证书 3-帖子图片 4-评论图片 5-项目申请附件 6-人才卡片附件 7-用户头像 8-认证证明材料", required = true, 
                      allowableValues = "1,2,3,4,5,6,7,8")
            @RequestParam("targetType") Integer targetType,
            @ApiParam(value = "是否临时文件（true 表示上传后暂未关联业务）", defaultValue = "false")
            @RequestParam(value = "isTemp", required = false, defaultValue = "false") Boolean isTemp) {
        
        log.info("收到文件上传请求，文件名：{}, 类型：{}", file.getOriginalFilename(), targetType);
        
        try {
            // 获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            // 调用服务上传文件
            FileUploadResponse response = fileService.uploadFile(file, targetType, isTemp, userId);
            log.info("文件上传成功，fileId: {}", response.getFileId());
            
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (BusinessException e) {
            log.error("文件上传失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取技能标签列表
     * @return 技能标签列表
     */
    @GetMapping("/skill-tags")
    @ApiOperation(value = "获取技能标签列表", notes = "获取系统预设的技能标签列表")
    public ResponseEntity<CommonResponse<List<SkillTag>>> getSkillTags() {
        log.info("获取技能标签列表");
        
        try {
            // 查询所有技能标签
            List<SkillTag> skillTags = skillTagMapper.selectList(null);
            log.info("获取技能标签列表成功，共 {} 个", skillTags.size());
            
            return ResponseEntity.ok(CommonResponse.ok(skillTags));
        } catch (Exception e) {
            log.error("获取技能标签列表失败", e);
            throw new BusinessException("获取技能标签列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/file/{fileId}")
    @ApiOperation(value = "获取文件信息", notes = "根据文件ID获取文件详细信息")
    public ResponseEntity<CommonResponse<FileInfoResponse>> getFileInfo(
            @ApiParam(value = "文件ID", required = true)
            @PathVariable("fileId") Long fileId) {
        
        log.info("获取文件信息，fileId: {}", fileId);
        
        try {
            FileInfoResponse response = fileService.getFileInfo(fileId);
            log.info("获取文件信息成功，fileId: {}", fileId);
            
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (BusinessException e) {
            log.error("获取文件信息失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取文件信息异常", e);
            throw new BusinessException("获取文件信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 删除响应
     */
    @DeleteMapping("/file/{fileId}")
    @ApiOperation(value = "删除文件", notes = "软删除文件")
    @NeedLogin
    public ResponseEntity<CommonResponse<FileDeleteResponse>> deleteFile(
            @ApiParam(value = "文件ID", required = true)
            @PathVariable("fileId") Long fileId) {
        
        log.info("删除文件，fileId: {}", fileId);
        
        try {
            // 获取当前登录用户 ID
            Integer userId = UserContextUtil.getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }
            
            // 调用服务删除文件
            fileService.deleteFile(fileId, userId);
            
            // 构建响应
            FileDeleteResponse response = new FileDeleteResponse();
            response.setMessage("文件已删除");
            
            log.info("文件删除成功，fileId: {}", fileId);
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (BusinessException e) {
            log.error("删除文件失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除文件异常", e);
            throw new BusinessException("删除文件失败：" + e.getMessage());
        }
    }
}