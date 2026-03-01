package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Favorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表操作Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    // 收藏表操作方法
}