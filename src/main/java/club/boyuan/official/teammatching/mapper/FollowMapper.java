package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Follow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注表操作Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    // 关注表操作方法
}