package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.AuthMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 身份认证材料 Mapper
 */
@Mapper
public interface AuthMaterialMapper extends BaseMapper<AuthMaterial> {
    // 身份认证材料操作方法
}
