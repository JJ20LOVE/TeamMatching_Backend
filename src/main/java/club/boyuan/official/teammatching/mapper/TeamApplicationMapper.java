package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.TeamApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入队申请表操作Mapper
 */
@Mapper
public interface TeamApplicationMapper extends BaseMapper<TeamApplication> {
    // 入队申请表操作方法
}