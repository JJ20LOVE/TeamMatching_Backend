package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.ProjectRoleRequirements;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目角色要求表操作Mapper
 */
@Mapper
public interface ProjectRoleRequirementMapper extends BaseMapper<ProjectRoleRequirements> {
    // 项目角色要求表操作方法
}