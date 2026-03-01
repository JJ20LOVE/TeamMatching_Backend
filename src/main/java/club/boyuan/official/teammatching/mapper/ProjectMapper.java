package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目表操作Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    // 项目表操作方法
}