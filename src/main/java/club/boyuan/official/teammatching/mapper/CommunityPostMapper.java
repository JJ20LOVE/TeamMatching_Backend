package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.CommunityPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区帖子表操作Mapper
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
    // 社区帖子表操作方法
}