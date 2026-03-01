package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.UserSkillRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-技能关联表操作Mapper
 */
@Mapper
public interface UserSkillRelationMapper extends BaseMapper<UserSkillRelation> {
    // 用户-技能关联表操作方法
}