package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.TeamMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队成员表操作Mapper
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
    // 团队成员表操作方法
}