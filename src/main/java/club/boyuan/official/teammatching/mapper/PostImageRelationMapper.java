package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.PostImageRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子图片关联表操作 Mapper
 */
@Mapper
public interface PostImageRelationMapper extends BaseMapper<PostImageRelation> {
    // 帖子图片关联表操作方法
}
