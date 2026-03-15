package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.LikeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞表操作Mapper
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    // 点赞表操作方法
}
