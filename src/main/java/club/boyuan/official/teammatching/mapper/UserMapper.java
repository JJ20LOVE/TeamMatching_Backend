package club.boyuan.official.teammatching.mapper;

import club.boyuan.official.teammatching.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表操作Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 用户表操作方法
}